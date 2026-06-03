package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    PREPAID("선결제", 1.0),
    // TODO: 예약금 PG 결제 도입 시 정책에 맞게 결제 비율을 조정한다.
    ON_SITE("현장결제", 0.0);

    private final String description;
    private final double paymentRatio;
}
