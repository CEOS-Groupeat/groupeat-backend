package com.groupeat.domain.terms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TermsAgreementUpdateRequest(
        @Schema(description = "변경할 동의 상태. true는 동의, false는 철회", example = "false")
        @NotNull(message = "약관 동의 여부는 필수입니다.") Boolean agreed
) {
}
