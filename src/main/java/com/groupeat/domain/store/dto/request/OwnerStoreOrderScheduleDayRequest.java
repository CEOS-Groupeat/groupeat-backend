package com.groupeat.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record OwnerStoreOrderScheduleDayRequest(

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

        @Schema(description = "주문 가능 여부. false이면 해당 요일을 휴무 처리합니다.", example = "true")
        @NotNull(message = "주문 가능 여부는 필수입니다.")
        Boolean available,

        @Schema(description = "최소 주문 수량. available=true일 때 필수입니다.", example = "10")
        @Min(value = 1, message = "최소 주문 수량은 1 이상이어야 합니다.")
        Integer minOrderQuantity,

        @Schema(description = "최대 주문 수량. available=true일 때 필수입니다.", example = "100")
        @Min(value = 1, message = "최대 주문 수량은 1 이상이어야 합니다.")
        Integer maxOrderQuantity,

        @Schema(description = "픽업 가능 시간 구간 목록. available=true일 때 최소 1개 이상 필요합니다.")
        @Valid
        List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> pickupTimeRanges,

        @Schema(description = "휴게 시간 구간 목록. 비어 있거나 null이면 휴게시간 없음으로 처리합니다.")
        @Valid
        List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> breakTimeRanges
) {
}
