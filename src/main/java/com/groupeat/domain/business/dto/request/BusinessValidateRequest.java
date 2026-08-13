package com.groupeat.domain.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BusinessValidateRequest(
        @NotBlank(message = "사업자등록번호는 필수 입력값입니다.")
        @Schema(description = "검증할 사업자등록번호 (하이픈 제외)", example = "1234567890")
        String businessNumber
) {}
