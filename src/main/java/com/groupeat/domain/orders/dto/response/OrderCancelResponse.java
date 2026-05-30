package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderCancelResponse(
        Long orderId,
        OrderStatus orderStatus,
        Integer refundRate,
        Integer refundAmount,
        LocalDateTime cancelledAt
) {
}
