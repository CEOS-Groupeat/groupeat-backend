package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderListFilterType {
    ALL("전체", null),
    IN_PROGRESS("진행 중", List.of(OrderStatus.PAID, OrderStatus.ACCEPTED)),
    PAST("과거 내역", List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED, OrderStatus.CANCELLED));

    private final String description;
    private final List<OrderStatus> mappedStatuses;
}