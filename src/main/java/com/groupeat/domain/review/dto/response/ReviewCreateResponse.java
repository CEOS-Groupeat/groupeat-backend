package com.groupeat.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record ReviewCreateResponse(

        @Schema(description = "생성된 리뷰 ID", example = "1")
        Long reviewId,

        @Schema(description = "리뷰 작성 날짜", example = "2026-07-03")
        LocalDate createdAtDate,

        @Schema(description = "리뷰 작성 시간", example = "14:00:00")
        LocalTime createdAtTime
) {
}
