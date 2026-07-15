package com.groupeat.domain.notification.event;

public record NewOrderRequestNotificationEvent(
        Long orderId
) {
}
