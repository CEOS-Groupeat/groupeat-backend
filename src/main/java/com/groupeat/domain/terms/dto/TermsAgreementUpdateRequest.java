package com.groupeat.domain.terms.dto;

import jakarta.validation.constraints.NotNull;

public record TermsAgreementUpdateRequest(
        @NotNull(message = "약관 동의 여부는 필수입니다.") Boolean agreed
) {
}
