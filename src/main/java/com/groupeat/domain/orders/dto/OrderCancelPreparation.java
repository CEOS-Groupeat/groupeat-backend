package com.groupeat.domain.orders.dto;

import com.groupeat.domain.payment.entity.Payment;

public record OrderCancelPreparation(
        int refundRate,
        int refundAmount,
        Payment payment
) {
}
