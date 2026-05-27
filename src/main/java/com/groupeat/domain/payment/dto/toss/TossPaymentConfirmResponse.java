package com.groupeat.domain.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        String method,
        Integer totalAmount,
        Integer balanceAmount,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt
) {
}
