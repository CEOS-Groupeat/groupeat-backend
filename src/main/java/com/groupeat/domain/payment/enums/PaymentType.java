package com.groupeat.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
    PREPAID("선결제"),
    ON_SITE("현장결제");

    private final String description;
}
