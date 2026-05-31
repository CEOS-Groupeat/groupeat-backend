package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusChangeResponse(
        Long orderId,
        OrderStatus orderStatus,
        LocalDateTime processedAt
) {
}
