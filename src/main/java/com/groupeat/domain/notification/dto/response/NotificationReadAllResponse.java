package com.groupeat.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationReadAllResponse(
        @Schema(description = "읽음 처리된 알림 개수", example = "5")
        int updatedCount
) {
}
