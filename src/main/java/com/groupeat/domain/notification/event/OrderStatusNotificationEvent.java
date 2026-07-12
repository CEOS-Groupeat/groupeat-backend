package com.groupeat.domain.notification.event;

import com.groupeat.domain.orders.enums.OrderStatus;

public record OrderStatusNotificationEvent(
        Long orderId,
        Long memberId,
        OrderStatus orderStatus,
        String storeName
) {
}
