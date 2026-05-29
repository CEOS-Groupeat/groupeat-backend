package com.groupeat.domain.payment.dto;

import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;

public record PreparedPaymentConfirm(
        Long paymentId,
        String orderId,
        Integer paidAmount,
        PaymentConfirmResponse alreadyConfirmedResponse
) {

    public static PreparedPaymentConfirm ready(Long paymentId, String orderId, Integer paidAmount) {
        return new PreparedPaymentConfirm(paymentId, orderId, paidAmount, null);
    }

    public static PreparedPaymentConfirm alreadyConfirmed(PaymentConfirmResponse response) {
        return new PreparedPaymentConfirm(null, null, null, response);
    }
}
