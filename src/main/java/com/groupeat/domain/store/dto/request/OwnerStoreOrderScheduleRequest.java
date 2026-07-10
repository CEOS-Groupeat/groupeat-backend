package com.groupeat.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record OwnerStoreOrderScheduleRequest(

        @Schema(description = "운영정보 적용 시작일", example = "2026-05-20")
        @NotNull(message = "운영정보 적용 시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "운영정보 적용 종료일", example = "2027-05-20")
        @NotNull(message = "운영정보 적용 종료일은 필수입니다.")
        LocalDate endDate,

        @Schema(description = "최소 주문 가능 기한(픽업 n일 전까지 주문 가능). 가게 공통 설정값입니다.", example = "3")
        @NotNull(message = "최소 주문 가능 기한은 필수입니다.")
        @Min(value = 0, message = "최소 주문 가능 기한은 0 이상이어야 합니다.")
        Integer minimumOrderDeadlineDays,

        @Schema(description = "요일별 운영정보 최종 상태. MONDAY~SUNDAY가 중복 없이 모두 포함되어야 합니다.")
        @Valid
        @Size(min = 7, max = 7, message = "요일별 운영정보는 7개여야 합니다.")
        List<DailyScheduleRequest> dailySchedules
) {

    @Builder
    public record DailyScheduleRequest(
            @Schema(description = "요일", example = "MONDAY")
            @NotNull(message = "요일은 필수입니다.")
            DayOfWeek dayOfWeek,

            @Schema(description = "주문 가능 여부. false이면 해당 요일을 휴무 처리합니다.", example = "true")
            @NotNull(message = "주문 가능 여부는 필수입니다.")
            Boolean available,

            @Schema(description = "최소 주문 수량. available=true일 때 필수입니다.", example = "10")
            @Min(value = 1, message = "최소 주문 수량은 1 이상이어야 합니다.")
            Integer minOrderQuantity,

            @Schema(description = "최대 주문 수량. available=true일 때 필수입니다.", example = "100")
            @Min(value = 1, message = "최대 주문 수량은 1 이상이어야 합니다.")
            Integer maxOrderQuantity,

            @Schema(description = "영업/픽업 가능 시간. available=true일 때 필수입니다.")
            @Valid
            TimeRangeRequest pickupTimeRange,

            @Schema(description = "휴게 시간. 비어 있거나 null이면 휴게시간 없음으로 처리합니다.")
            @Valid
            TimeRangeRequest breakTimeRange
    ) {
    }

    @Builder
    public record TimeRangeRequest(
            @Schema(description = "시작 시간", example = "10:00")
            @NotNull(message = "시작 시간은 필수입니다.")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "17:00")
            @NotNull(message = "종료 시간은 필수입니다.")
            LocalTime endTime
    ) {
    }
}
