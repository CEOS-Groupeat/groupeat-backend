package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record OrderStatusChangeResponse(
        Long orderId,
        OrderStatus orderStatus,
        LocalDate processedDate,
        LocalTime processedTime
) {
}
