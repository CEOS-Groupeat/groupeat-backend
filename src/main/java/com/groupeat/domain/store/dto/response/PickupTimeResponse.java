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

        @Schema(description = "총 주문 가능 수량", example = "100")
        Integer dailyAvailableQuantity,

        @Schema(description = "픽업 시작 시간", example = "10:00")
        LocalTime openTime,

        @Schema(description = "픽업 종료 시간", example = "17:00")
        LocalTime closeTime,

        @Schema(description = "시간 간격(분)", example = "30")
        Integer intervalMinutes

) {}