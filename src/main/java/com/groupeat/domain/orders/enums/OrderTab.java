package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderTab {

    WAITING(List.of(OrderStatus.PAID), false),
    CONFIRMED(List.of(OrderStatus.ACCEPTED), true),
    PAST(List.of(OrderStatus.COMPLETED, OrderStatus.REJECTED, OrderStatus.CANCELLED), false);

    private final List<OrderStatus> statuses;
    private final boolean isConfirmedTab;
}
