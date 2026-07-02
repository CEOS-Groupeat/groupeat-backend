package com.groupeat.domain.admin.business.dto.response;

import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record AdminVerificationProcessResponse(
        @Schema(description = "처리된 사업자 프로필 ID", example = "15")
        Long businessProfileId,

        @Schema(description = "처리 후 최종 상태 (APPROVED / REJECTED)", example = "APPROVED")
        BusinessVerificationStatus status,

        @Schema(description = "승인/반려 심사 완료 날짜", example = "2026-06-23")
        LocalDate reviewedDate,

        @Schema(description = "승인/반려 심사 완료 일시", example = "14:30:00")
        LocalTime reviewedTime
) {
}