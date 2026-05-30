package com.groupeat.domain.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TossPaymentCancelRequest(
        String cancelReason,
        Integer cancelAmount
) {
    public static TossPaymentCancelRequest of(String cancelReason, Integer cancelAmount) {
        return new TossPaymentCancelRequest(cancelReason, cancelAmount);
    }
}
