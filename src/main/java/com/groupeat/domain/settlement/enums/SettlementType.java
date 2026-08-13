package com.groupeat.domain.settlement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementType {
    PAYOUT("점주 정산"),
    FEE_CHARGE("점주 수수료 청구");

    private final String description;
}
