package com.groupeat.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentProvider {
    TOSS("토스페이먼츠");

    private final String description;
}
