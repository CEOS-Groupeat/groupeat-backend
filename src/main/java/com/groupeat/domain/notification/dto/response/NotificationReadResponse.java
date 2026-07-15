package com.groupeat.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationReadResponse(
        @Schema(description = "알림 ID", example = "1")
        Long notificationId,

        @Schema(description = "읽음 여부", example = "true")
        boolean read,

        @Schema(description = "읽음 처리 시각", example = "2026-07-15T16:30:00")
        LocalDateTime readAt
) {
}
