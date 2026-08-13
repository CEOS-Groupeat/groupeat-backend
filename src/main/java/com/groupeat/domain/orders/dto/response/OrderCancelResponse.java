package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record OrderCancelResponse(
        Long orderId,
        OrderStatus orderStatus,
        Integer refundRate,
        Integer refundAmount,
        LocalDate cancelledDate,
        LocalTime cancelledTime
) {
}
