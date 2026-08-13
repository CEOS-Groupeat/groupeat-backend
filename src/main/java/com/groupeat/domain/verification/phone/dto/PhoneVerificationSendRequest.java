package com.groupeat.domain.verification.phone.dto;

import jakarta.validation.constraints.NotBlank;

public record PhoneVerificationSendRequest(
        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        String phoneNumber
) {
}
