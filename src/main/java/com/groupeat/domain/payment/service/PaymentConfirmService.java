package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.enums.TossPaymentStatus;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.exception.TossPaymentException;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private static final String INVALID_CONFIRM_RESPONSE_CODE = "INVALID_TOSS_CONFIRM_RESPONSE";

    private final TossPaymentClient tossPaymentClient;
    private final PaymentConfirmTransactionService paymentConfirmTransactionService;
    private final PaymentReconciliationService paymentReconciliationService;

    public PaymentConfirmResponse confirm(Long memberId, String idempotencyKey, PaymentConfirmRequest request) {
        PreparedPaymentConfirm preparedPayment = paymentConfirmTransactionService.prepareConfirm(memberId, idempotencyKey, request);
        if (preparedPayment.alreadyConfirmedResponse() != null) {
            return preparedPayment.alreadyConfirmedResponse();
        }

        TossPaymentConfirmResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.confirmPayment(
                    TossPaymentConfirmRequest.from(request),
                    idempotencyKey
            );
        } catch (TossPaymentException e) {
            return handleConfirmFailure(preparedPayment, request.paymentKey(), e);
        } catch (RestClientException e) {
            return paymentReconciliationService.reconcilePayment(preparedPayment, request.paymentKey(), e);
        }

        if (tossResponse == null) {
            return paymentReconciliationService.reconcilePayment(preparedPayment, request.paymentKey(),
                    new IllegalStateException("토스 승인 응답이 비어 있습니다."));
        }

        validateTossConfirmResponse(tossResponse, preparedPayment);
        try {
            return paymentConfirmTransactionService.approvePayment(preparedPayment.paymentId(), tossResponse);
        } catch (RuntimeException e) {
            return paymentReconciliationService.reconcilePayment(preparedPayment, request.paymentKey(), e);
        }
    }

    // 토스 승인 오류를 분류하여 처리 중·확정 실패·결과 재조회로 분기
    private PaymentConfirmResponse handleConfirmFailure(
            PreparedPaymentConfirm preparedPayment, String paymentKey, TossPaymentException e
    ) {
        if (e.isProcessing()) {
            throw new GeneralException(PaymentErrorStatus.PAYMENT_CONFIRM_IN_PROGRESS);
        }

        // 토스결제 실패 케이스
        if (e.isDefinitiveFailure()) {
            paymentConfirmTransactionService.failPayment(
                    preparedPayment.paymentId(), e.getTossErrorCode(), e.getTossErrorMessage()
            );
            throw new GeneralException(PaymentErrorStatus.TOSS_CONFIRM_FAILED);
        }

        return paymentReconciliationService.reconcilePayment(preparedPayment, paymentKey, e);
    }

    // 토스 승인 성공 응답이 우리 결제 요청 정보와 일치하는지 검증
    private void validateTossConfirmResponse(TossPaymentConfirmResponse tossResponse, PreparedPaymentConfirm preparedPayment) {
        if (TossPaymentStatus.from(tossResponse.status()) != TossPaymentStatus.DONE
                || !preparedPayment.orderId().equals(tossResponse.orderId())
                || !preparedPayment.paidAmount().equals(tossResponse.totalAmount())) {
            // TODO: 결제 실패 보상 PR에서 토스 승인 성공 후 내부 검증 실패 시 취소/환불 처리 추가
            paymentConfirmTransactionService.failPayment(preparedPayment.paymentId(), INVALID_CONFIRM_RESPONSE_CODE, "토스페이먼츠 결제 승인 응답이 결제 요청 정보와 일치하지 않습니다.");
            throw new GeneralException(PaymentErrorStatus.TOSS_CONFIRM_FAILED);
        }
    }
}
