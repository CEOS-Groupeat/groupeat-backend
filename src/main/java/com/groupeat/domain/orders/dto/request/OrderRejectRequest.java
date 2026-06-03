package com.groupeat.domain.orders.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderRejectRequest(
        @Schema(description = "거절 사유", example = "재료 소진")
        @NotBlank(message = "거절 사유는 필수입니다.")
        @Size(max = 100, message = "거절 사유는 100자 이하로 입력해주세요.")
        String rejectReason
) {
}
