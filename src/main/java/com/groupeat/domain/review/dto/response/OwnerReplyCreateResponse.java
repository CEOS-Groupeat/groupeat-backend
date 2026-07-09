package com.groupeat.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Schema(name = "OwnerReplyCreateResponse", description = "사장님 답글 작성 성공 응답")
public record OwnerReplyCreateResponse(
        @Schema(description = "답글이 작성된 리뷰 ID", example = "1")
        Long reviewId,

        @Schema(description = "답글 작성 날짜", example = "2026-07-07")
        LocalDate repliedAtDate,

        @Schema(description = "답글 작성 시간", example = "23:45:00")
        LocalTime repliedAtTime
) {}