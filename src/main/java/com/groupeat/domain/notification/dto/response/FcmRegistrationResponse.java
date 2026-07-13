package com.groupeat.domain.notification.dto.response;

import com.groupeat.domain.notification.entity.FcmRegistration;
import com.groupeat.domain.notification.enums.FcmPlatform;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FcmRegistrationResponse(
        @Schema(description = "FCM 등록 ID", example = "1")
        Long fcmRegistrationId,

        @Schema(description = "FCM 등록 플랫폼", example = "WEB")
        FcmPlatform platform,

        @Schema(description = "활성화 여부", example = "true")
        boolean active,

        @Schema(description = "마지막 등록 시각", example = "2026-07-13T14:30:00")
        LocalDateTime lastRegisteredAt
) {
    public static FcmRegistrationResponse from(FcmRegistration registration) {
        return new FcmRegistrationResponse(
                registration.getId(),
                registration.getPlatform(),
                registration.isActive(),
                registration.getLastRegisteredAt()
        );
    }
}
