package com.groupeat.domain.payment.dto.toss;

import com.groupeat.domain.payment.dto.request.PaymentConfirmRequest;

public record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {

    public static TossPaymentConfirmRequest from(PaymentConfirmRequest request) {
        return new TossPaymentConfirmRequest(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );
    }
}
