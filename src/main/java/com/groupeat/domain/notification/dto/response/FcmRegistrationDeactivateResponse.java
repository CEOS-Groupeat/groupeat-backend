package com.groupeat.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FcmRegistrationDeactivateResponse(
        @Schema(description = "실제 비활성화 처리 여부", example = "true")
        boolean deactivated
) {
}
