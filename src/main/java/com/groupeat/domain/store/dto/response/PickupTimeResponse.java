package com.groupeat.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record PickupTimeResponse(
        @Schema(description = "요청 날짜", example = "2026-04-23")
        LocalDate date,

        @Schema(description = "최소 주문 가능 수량", example = "10")
        Integer dailyMinOrderQuantity,

        @Schema(description = "총 주문 가능 수량", example = "100")
        Integer dailyAvailableQuantity,

        @Schema(description = "해당 날짜에 이미 승인된 주문 수량", example = "80")
        Integer dailyAcceptedQuantity,

        @Schema(description = "해당 날짜의 잔여 주문 가능 수량", example = "20")
        Integer dailyRemainingQuantity,

        @Schema(description = "시간 간격(분)", example = "30")
        Integer intervalMinutes,

        @Schema(description = "픽업 가능 시간 구간 목록")
        List<TimeRangeResponse> pickupTimeRanges,

        @Schema(description = "휴게 시간 구간 목록")
        List<TimeRangeResponse> breakTimeRanges

) {
    @Builder
    public record TimeRangeResponse(
            @Schema(description = "시작 시간", example = "10:00")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "17:00")
            LocalTime endTime
    ) {
    }
}
