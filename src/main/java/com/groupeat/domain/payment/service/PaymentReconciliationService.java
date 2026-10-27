package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentCancelRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.enums.TossPaymentStatus;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.exception.TossPaymentException;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private static final String RECONCILIATION_FAILED_CODE = "PAYMENT_RECONCILIATION_FAILED";
    private static final String APPROVAL_FAILURE_CANCEL_REASON = "결제 승인 후 주문 처리 실패로 자동 취소";

    private final TossPaymentClient tossPaymentClient;
    private final PaymentConfirmTransactionService paymentConfirmTransactionService;

    // 토스의 실제 결제 상태를 조회하고 상태에 맞게 내부 데이터를 복구
    public PaymentConfirmResponse reconcilePayment(
            PreparedPaymentConfirm preparedPayment, String paymentKey, RuntimeException initialFailure
    ) {
        log.warn("결제 상태 재조회 및 내부 반영 시도. paymentId={}, orderId={}, initialFailure={}",
                preparedPayment.paymentId(), preparedPayment.orderId(), describeFailure(initialFailure), initialFailure);

        TossPaymentConfirmResponse payment;
        try {
            payment = fetchValidatedPayment(preparedPayment, paymentKey);
        } catch (RuntimeException e) {
            throw reconciliationRequired(preparedPayment, initialFailure, e);
        }

        TossPaymentStatus status = TossPaymentStatus.from(payment.status());
        if (status == TossPaymentStatus.DONE) {
            return reconcileApprovedPayment(preparedPayment, payment, initialFailure);
        }
        if (status.isFailed()) {
            recordFailedPayment(preparedPayment, payment, initialFailure);
            throw new GeneralException(PaymentErrorStatus.TOSS_CONFIRM_FAILED);
        }

        throw reconciliationRequired(preparedPayment, initialFailure,
                new IllegalStateException("토스 결제 결과 확인이 필요합니다. status=" + payment.status()));
    }

    // 토스 결제를 조회하고 결제키·주문 ID·금액이 요청과 일치하는지 검증
    private TossPaymentConfirmResponse fetchValidatedPayment(
            PreparedPaymentConfirm preparedPayment, String paymentKey
    ) {
        TossPaymentConfirmResponse payment = tossPaymentClient.getPayment(paymentKey);
        if (payment == null
                || !paymentKey.equals(payment.paymentKey())
                || !preparedPayment.orderId().equals(payment.orderId())
                || !preparedPayment.paidAmount().equals(payment.totalAmount())) {
            throw new IllegalStateException("토스 재조회 결과가 결제 요청과 일치하지 않습니다.");
        }
        return payment;
    }

    // 확인된 승인을 DB에 반영하고, 반영 실패 시 전액 취소를 시도
    private PaymentConfirmResponse reconcileApprovedPayment(
            PreparedPaymentConfirm preparedPayment, TossPaymentConfirmResponse payment,
            RuntimeException initialFailure
    ) {
        try {
            return paymentConfirmTransactionService.approvePayment(preparedPayment.paymentId(), payment);
        } catch (RuntimeException e) {
            return cancelAfterApprovalFailure(preparedPayment, payment.paymentKey(), initialFailure, e);
        }
    }

    // 확인된 결제 실패를 DB에 반영하고, 저장 실패 시 복구 필요 상태를 기록
    private void recordFailedPayment(
            PreparedPaymentConfirm preparedPayment, TossPaymentConfirmResponse payment,
            RuntimeException initialFailure
    ) {
        try {
            paymentConfirmTransactionService.failPayment(
                    preparedPayment.paymentId(),
                    payment.failure() != null ? payment.failure().code() : payment.status(),
                    payment.failure() != null ? payment.failure().message() : "토스 결제 승인 실패 또는 만료"
            );
        } catch (RuntimeException e) {
            throw reconciliationRequired(preparedPayment, initialFailure, e);
        }
    }

    // 복구 필요 상태를 기록하고 호출부에서 던질 예외를 생성
    private GeneralException reconciliationRequired(
            PreparedPaymentConfirm preparedPayment, RuntimeException initialFailure, RuntimeException recoveryFailure
    ) {
        recordReconciliationRequired(preparedPayment, initialFailure, recoveryFailure);
        return new GeneralException(PaymentErrorStatus.PAYMENT_RECONCILIATION_REQUIRED);
    }

    // 승인 반영 여부를 재확인한 뒤 전액 취소하고, 취소 실패 시 복구 필요 상태를 기록
    private PaymentConfirmResponse cancelAfterApprovalFailure(
            PreparedPaymentConfirm preparedPayment, String paymentKey,
            RuntimeException initialFailure, RuntimeException approvalFailure
    ) {
        try {
            PaymentConfirmResponse confirmed = paymentConfirmTransactionService.prepareApprovalFailureCancel(
                    preparedPayment.paymentId(), paymentKey);
            if (confirmed != null) {
                return confirmed;
            }

            log.warn("결제 내부 반영 재시도 실패. 전액 취소를 시도합니다. paymentId={}, orderId={}",
                    preparedPayment.paymentId(), preparedPayment.orderId(), approvalFailure);
            TossPaymentConfirmResponse canceled = tossPaymentClient.cancelPayment(
                    paymentKey, TossPaymentCancelRequest.of(APPROVAL_FAILURE_CANCEL_REASON, null));
            validateFullCancelResponse(canceled, preparedPayment, paymentKey);

            PaymentCancelResult cancelResult = PaymentCancelResult.from(canceled, preparedPayment.paidAmount());
            log.info("토스 전액 취소 확인. paymentId={}, orderId={}, refundedAmount={}, transactionKey={}",
                    preparedPayment.paymentId(), preparedPayment.orderId(),
                    cancelResult.refundedAmount(), cancelResult.lastTransactionKey());
            paymentConfirmTransactionService.cancelAfterApprovalFailure(
                    preparedPayment.paymentId(), paymentKey, APPROVAL_FAILURE_CANCEL_REASON, cancelResult);
        } catch (RuntimeException cancelFailure) {
            RuntimeException recoveryFailure = new IllegalStateException(
                    "approvalFailure=%s; cancelFailure=%s"
                            .formatted(describeFailure(approvalFailure), describeFailure(cancelFailure)),
                    cancelFailure);
            recordReconciliationRequired(preparedPayment, initialFailure, recoveryFailure);
            throw new GeneralException(PaymentErrorStatus.PAYMENT_RECONCILIATION_REQUIRED);
        }

        throw new GeneralException(PaymentErrorStatus.PAYMENT_CANCELED_AFTER_CONFIRM_FAILURE);
    }

    // 전액 취소 응답의 결제 정보와 잔액을 검증
    private void validateFullCancelResponse(
            TossPaymentConfirmResponse response, PreparedPaymentConfirm preparedPayment, String paymentKey
    ) {
        if (response == null
                || TossPaymentStatus.from(response.status()) != TossPaymentStatus.CANCELED
                || !paymentKey.equals(response.paymentKey())
                || !preparedPayment.orderId().equals(response.orderId())
                || !preparedPayment.paidAmount().equals(response.totalAmount())
                || !Integer.valueOf(0).equals(response.balanceAmount())) {
            throw new IllegalStateException("토스 전액 취소 결과를 확인할 수 없습니다.");
        }
    }

    // 복구 필요 상태를 저장하고 저장 실패 시 로그를 기록
    private void recordReconciliationRequired(
            PreparedPaymentConfirm preparedPayment, RuntimeException initialFailure, RuntimeException recoveryFailure
    ) {
        String failureMessage = "initialFailure=%s; recoveryFailure=%s"
                .formatted(describeFailure(initialFailure), describeFailure(recoveryFailure));
        log.error("결제 복구 실패. paymentId={}, orderId={}, failures={}",
                preparedPayment.paymentId(), preparedPayment.orderId(), failureMessage, recoveryFailure);
        try {
            paymentConfirmTransactionService.markReconciliationRequired(
                    preparedPayment.paymentId(), RECONCILIATION_FAILED_CODE, failureMessage);
        } catch (RuntimeException recordException) {
            log.error("결제 복구 필요 상태 기록 실패. paymentId={}, orderId={}, failures={}",
                    preparedPayment.paymentId(), preparedPayment.orderId(), failureMessage, recordException);
        }
    }

    // 복구 로그에 사용할 오류 정보를 구성
    private String describeFailure(RuntimeException failure) {
        if (failure instanceof TossPaymentException tossFailure) {
            return "%s[%s]: %s".formatted(failure.getClass().getSimpleName(),
                    tossFailure.getTossErrorCode(), failure.getMessage());
        }
        return failure.getClass().getSimpleName() + ": " + failure.getMessage();
    }

}
