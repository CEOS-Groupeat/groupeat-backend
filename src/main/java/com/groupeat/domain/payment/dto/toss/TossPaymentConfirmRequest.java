package com.groupeat.domain.payment.dto.toss;

public record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {
}
