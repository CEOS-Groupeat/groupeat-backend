package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderListFilterType {
    ALL("전체", List.of()),
    ACCEPTED("승인 완료", List.of(OrderStatus.PAID, OrderStatus.ACCEPTED)),
    COMPLETED("픽업 완료", List.of(OrderStatus.COMPLETED)),
    REJECTED("거절", List.of(OrderStatus.REJECTED));

    private final String description;
    private final List<OrderStatus> mappedStatuses;
}