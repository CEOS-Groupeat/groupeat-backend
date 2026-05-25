package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    PREPAID("선결제", 1.0),
    ON_SITE("현장결제", 0.5);

    private final String description;
    private final double paymentRatio;
}
