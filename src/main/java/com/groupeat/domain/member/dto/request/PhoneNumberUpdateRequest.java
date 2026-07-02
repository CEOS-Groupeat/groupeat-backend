package com.groupeat.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PhoneNumberUpdateRequest(
        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        String phoneNumber
) {
}
