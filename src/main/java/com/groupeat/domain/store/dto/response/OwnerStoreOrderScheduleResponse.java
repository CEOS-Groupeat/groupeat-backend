package com.groupeat.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record OwnerStoreOrderScheduleResponse(

        @Schema(description = "주문 가능 일정 ID", example = "1")
        Long scheduleId,

        @Schema(description = "가게 ID", example = "1")
        Long storeId,

        @Schema(description = "일정 적용 시작일", example = "2026-05-20")
        LocalDate startDate,

        @Schema(description = "일정 적용 종료일", example = "2027-05-20")
        LocalDate endDate,

        @Schema(description = "최소 주문 가능 기한(픽업 n일 전까지 주문 가능)", example = "3")
        Integer minOrderDays,

        @Schema(description = "요일별 주문 가능 일정")
        List<DayScheduleResponse> days
) {

    @Builder
    public record DayScheduleResponse(
            @Schema(description = "요일", example = "MONDAY")
            DayOfWeek dayOfWeek,

            @Schema(description = "주문 가능 여부", example = "true")
            boolean available,

            @Schema(description = "최소 주문 수량", example = "10")
            Integer minOrderQuantity,

            @Schema(description = "최대 주문 수량", example = "100")
            Integer maxOrderQuantity,

            @Schema(description = "픽업 시간 간격(분)", example = "30")
            Integer intervalMinutes,

            @Schema(description = "픽업 가능 시간 구간 목록")
            List<TimeRangeResponse> pickupTimeRanges,

            @Schema(description = "휴게 시간 구간 목록")
            List<TimeRangeResponse> breakTimeRanges
    ) {
    }

    @Builder
    public record TimeRangeResponse(
            @Schema(description = "시작 시간", example = "10:00")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "17:00")
            LocalTime endTime
    ) {
    }
}
