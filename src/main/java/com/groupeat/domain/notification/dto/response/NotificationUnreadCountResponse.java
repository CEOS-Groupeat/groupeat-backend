package com.groupeat.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationUnreadCountResponse(
        @Schema(description = "미읽음 알림 개수", example = "3")
        long unreadCount
) {
}
