package com.groupeat.domain.settlement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    PENDING("정산 대기"),
    COMPLETED("정산 완료"),
    CANCELED("정산 취소");

    private final String description;
}
