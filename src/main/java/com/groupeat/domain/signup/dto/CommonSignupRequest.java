package com.groupeat.domain.signup.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CommonSignupRequest(
        @NotBlank(message = "회원가입 토큰은 필수입니다.")
        String signupToken,

        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        String phoneNumber,

        @NotEmpty(message = "약관 동의 정보는 필수입니다.")
        List<@Valid SignupAgreementRequest> agreements
) {
}
