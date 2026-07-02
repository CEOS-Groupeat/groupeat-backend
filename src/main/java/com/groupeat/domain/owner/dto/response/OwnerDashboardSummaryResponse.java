package com.groupeat.domain.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record OwnerDashboardSummaryResponse(
        @Schema(description = "승인 대기 주문 건수", example = "3")
        long waitingCount,

        @Schema(description = "확정 주문 건수", example = "5")
        long confirmedCount,

        @Schema(description = "픽업 완료 건수", example = "8")
        long completedCount
) {}