package com.groupeat.domain.admin.business.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AdminVerificationProcessRequest(
        @Schema(description = "사업자 처리 여부", example = "true: 승인, false: 반려")
        @NotNull(message = "처리 상태는 필수입니다.")
        Boolean isApprove,

        @Schema(description = "반려일 경우 반려 이유")
        String rejectReason // 반려일 경우 필수
) {
}
