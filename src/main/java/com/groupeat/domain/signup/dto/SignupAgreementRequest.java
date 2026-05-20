package com.groupeat.domain.signup.dto;

import jakarta.validation.constraints.NotNull;

public record SignupAgreementRequest(
        @NotNull(message = "약관 ID는 필수입니다.")
        Long termsId,

        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreed
) {
}
