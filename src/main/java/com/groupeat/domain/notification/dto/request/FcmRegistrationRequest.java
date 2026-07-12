package com.groupeat.domain.notification.dto.request;

import com.groupeat.domain.notification.enums.FcmPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FcmRegistrationRequest(
        @NotBlank(message = "FCM 등록 토큰은 필수입니다.")
        @Schema(description = "Firebase Web SDK에서 발급한 FCM registration token")
        String registrationToken,

        @NotNull(message = "FCM 플랫폼은 필수입니다.")
        @Schema(description = "FCM 등록 플랫폼", example = "WEB")
        FcmPlatform platform
) {
}
