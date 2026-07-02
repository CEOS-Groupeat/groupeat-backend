package com.groupeat.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PhoneNumberUpdateRequest(
        @Schema(description = "인증 완료된 새 휴대폰 번호", example = "01098765432")
        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        String phoneNumber
) {
}
