package com.groupeat.domain.payment.converter;

import com.groupeat.domain.payment.dto.response.PaymentConfirmResponse;
import com.groupeat.domain.payment.entity.Payment;

public class PaymentConverter {

    public static PaymentConfirmResponse toConfirmResponse(Payment payment) {
        return new PaymentConfirmResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getPaidAmount(),
                payment.getPaymentStatus(),
                payment.getApprovedAt() == null ? null : payment.getApprovedAt().toLocalDate(),
                payment.getApprovedAt() == null ? null : payment.getApprovedAt().toLocalTime()
        );
    }
}
