package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PreparedPaymentConfirm;
import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;
import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.exception.TossPaymentException;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private static final String TOSS_DONE_STATUS = "DONE";

    private final TossPaymentClient tossPaymentClient;
    private final PaymentConfirmTransactionService paymentConfirmTransactionService;

    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request) {
        PreparedPaymentConfirm preparedPayment = paymentConfirmTransactionService.prepareConfirm(memberId, request);
        if (preparedPayment.alreadyConfirmedResponse() != null) {
            return preparedPayment.alreadyConfirmedResponse();
        }

        try {
            TossPaymentConfirmResponse tossResponse = tossPaymentClient.confirmPayment(
                    TossPaymentConfirmRequest.from(request)
            );
            validateTossConfirmResponse(tossResponse, preparedPayment);
            return paymentConfirmTransactionService.approvePayment(preparedPayment.paymentId(), tossResponse);
        } catch (TossPaymentException e) {
            // TODO: 결제 실패 보상 PR에서 승인 실패/타임아웃/재조회 정책을 구체화
            paymentConfirmTransactionService.failPayment(preparedPayment.paymentId(), e.getTossErrorCode(), e.getTossErrorMessage());
            throw new GeneralException(PaymentErrorStatus.TOSS_CONFIRM_FAILED);
        }
    }

    // 토스 승인 성공 응답이 우리 결제 요청 정보와 일치하는지 검증
    private void validateTossConfirmResponse(TossPaymentConfirmResponse tossResponse, PreparedPaymentConfirm preparedPayment) {
        if (!TOSS_DONE_STATUS.equals(tossResponse.status())
                || !preparedPayment.orderId().equals(tossResponse.orderId())
                || !preparedPayment.paidAmount().equals(tossResponse.totalAmount())) {
            // TODO: 결제 실패 보상 PR에서 토스 승인 성공 후 내부 검증 실패 시 취소/환불 처리 추가
            paymentConfirmTransactionService.failPayment(preparedPayment.paymentId(), "INVALID_TOSS_CONFIRM_RESPONSE", "토스페이먼츠 결제 승인 응답이 결제 요청 정보와 일치하지 않습니다.");
            throw new GeneralException(PaymentErrorStatus.TOSS_CONFIRM_FAILED);
        }
    }
}
