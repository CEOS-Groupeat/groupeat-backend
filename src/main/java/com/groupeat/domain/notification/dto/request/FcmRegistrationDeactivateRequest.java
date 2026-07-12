package com.groupeat.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record FcmRegistrationDeactivateRequest(
        @NotBlank(message = "FCM 등록 토큰은 필수입니다.")
        @Schema(description = "비활성화할 FCM registration token")
        String registrationToken
) {
}
