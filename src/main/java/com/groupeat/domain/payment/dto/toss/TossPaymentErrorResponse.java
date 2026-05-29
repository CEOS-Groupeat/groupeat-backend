package com.groupeat.domain.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentErrorResponse(
        String code,
        String message
) {
}
