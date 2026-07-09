package com.groupeat.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(name = "RecommendationResponse", description = "메인 화면 가게 추천 응답")
public record RecommendationResponse(

        @Schema(description = "인기 만점 가게 목록 (별점 높은 순, 최대 2개)")
        List<RecommendationCardDTO> topRatedStores,

        @Schema(description = "할인율 높은 가게 목록 (할인율 높은 순, 최대 2개)")
        List<RecommendationCardDTO> highDiscountStores
) {
    @Builder
    @Schema(name = "RecommendationCardDTO", description = "추천 가게 단일 카드 정보")
    public record RecommendationCardDTO(

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게 대표 이미지 URL", example = "https://groupeat-bucket.../store.jpg")
            String imageUrl,

            @Schema(description = "카테고리명", example = "샌드위치&김밥")
            String category,

            @Schema(description = "가게명", example = "사르르 연남")
            String storeName,

            @Schema(description = "구 정보 (위치)", example = "마포구")
            String district,

            @Schema(description = "동 정보 (위치)", example = "연남동")
            String neighborhood,

            @Schema(description = "최소 가격", example = "5500")
            Integer minPrice,

            @Schema(description = "최대 가격", example = "9000")
            Integer maxPrice,

            @Schema(description = "별점", example = "4.8")
            Double rating,

            @Schema(description = "리뷰 수", example = "45")
            Integer reviewCount
    ) {}
}