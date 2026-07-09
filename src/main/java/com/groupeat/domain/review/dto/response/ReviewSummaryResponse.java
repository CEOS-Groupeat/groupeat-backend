package com.groupeat.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "ReviewSummaryResponse", description = "사장님 리뷰 관리 최상단 요약 정보")
public record ReviewSummaryResponse(
        @Schema(description = "가게 이름", example = "데이브런치")
        String storeName,

        @Schema(description = "평균 별점", example = "4.7")
        double averageRating,

        @Schema(description = "총 리뷰 개수", example = "34")
        int totalReviewCount,

        @Schema(description = "5점 리뷰 개수", example = "30")
        int rating5Count,

        @Schema(description = "4점 리뷰 개수", example = "1")
        int rating4Count,

        @Schema(description = "3점 리뷰 개수", example = "0")
        int rating3Count,

        @Schema(description = "2점 리뷰 개수", example = "0")
        int rating2Count,

        @Schema(description = "1점 리뷰 개수", example = "0")
        int rating1Count
) {}
