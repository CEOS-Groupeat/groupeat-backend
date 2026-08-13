package com.groupeat.domain.payment.service;

import com.groupeat.domain.payment.client.TossPaymentClient;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.dto.toss.TossPaymentCancelRequest;
import com.groupeat.domain.payment.dto.toss.TossPaymentConfirmResponse;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.exception.PaymentErrorStatus;
import com.groupeat.domain.payment.exception.TossPaymentException;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final TossPaymentClient tossPaymentClient;

    public PaymentCancelResult cancel(Payment payment, String cancelReason, Integer refundAmount) {
        if (!isTossCancelable(payment, refundAmount)) {
            return PaymentCancelResult.skipped();
        }

        try {
            Integer cancelAmount = isFullCancel(payment, refundAmount) ? null : refundAmount;
            TossPaymentConfirmResponse response = tossPaymentClient.cancelPayment(
                    payment.getPaymentKey(),
                    TossPaymentCancelRequest.of(cancelReason, cancelAmount)
            );
            return PaymentCancelResult.from(response, refundAmount);
        } catch (TossPaymentException e) {
            throw new GeneralException(PaymentErrorStatus.TOSS_CANCEL_FAILED);
        }
    }

    private boolean isTossCancelable(Payment payment, Integer refundAmount) {
        return payment != null
                && refundAmount != null
                && refundAmount > 0
                && payment.getPaymentKey() != null
                && payment.getPaymentStatus() == PaymentStatus.DONE;
    }

    private boolean isFullCancel(Payment payment, Integer refundAmount) {
        return refundAmount >= payment.getPaidAmount();
    }
}
