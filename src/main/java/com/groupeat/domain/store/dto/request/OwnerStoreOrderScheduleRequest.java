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

        @Schema(description = "일정 적용 시작일", example = "2026-05-20")
        @NotNull(message = "일정 적용 시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "일정 적용 종료일", example = "2027-05-20")
        @NotNull(message = "일정 적용 종료일은 필수입니다.")
        LocalDate endDate,

        @Schema(description = "최소 주문 가능 기한(픽업 n일 전까지 주문 가능)", example = "3")
        @NotNull(message = "최소 주문 가능 기한은 필수입니다.")
        @Min(value = 0, message = "최소 주문 가능 기한은 0 이상이어야 합니다.")
        Integer minOrderDays,

        @Schema(description = "요일별 주문 가능 일정. 포함되지 않은 요일은 휴무 처리됩니다.")
        @Valid
        @Size(max = 7, message = "요일별 일정은 최대 7개까지 설정할 수 있습니다.")
        List<DayScheduleRequest> days
) {

    @Builder
    public record DayScheduleRequest(
            @Schema(description = "요일", example = "MONDAY")
            @NotNull(message = "요일은 필수입니다.")
            DayOfWeek dayOfWeek,

            @Schema(description = "주문 가능 여부", example = "true")
            @NotNull(message = "주문 가능 여부는 필수입니다.")
            Boolean available,

            @Schema(description = "최소 주문 수량", example = "10")
            @Min(value = 1, message = "최소 주문 수량은 1 이상이어야 합니다.")
            Integer minOrderQuantity,

            @Schema(description = "최대 주문 수량", example = "100")
            @Min(value = 1, message = "최대 주문 수량은 1 이상이어야 합니다.")
            Integer maxOrderQuantity,

            @Schema(description = "픽업 시작 시간", example = "10:00")
            LocalTime pickupOpenTime,

            @Schema(description = "픽업 종료 시간", example = "17:00")
            LocalTime pickupCloseTime,

            @Schema(description = "픽업 시간 간격(분)", example = "30")
            @Min(value = 1, message = "픽업 시간 간격은 1분 이상이어야 합니다.")
            Integer intervalMinutes
    ) {
    }
}
