package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCancelledBy {
    CUSTOMER("고객"),
    OWNER("사업자"),
    SYSTEM("시스템");

    private final String description;
}
