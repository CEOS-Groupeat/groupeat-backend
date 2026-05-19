package com.groupeat.domain.verification.phone.dto;

import jakarta.validation.constraints.NotBlank;

public record PhoneVerificationConfirmRequest(
        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        String phoneNumber,

        @NotBlank(message = "인증번호는 필수입니다.")
        String code
) {
}
