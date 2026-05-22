package com.groupeat.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

public class StoreSearchResponse {

    @Builder
    public record StoreListDTO(
            @Schema(description = "검색된 전체 가게 수", example = "42")
            long totalElements,

            @Schema(description = "가게 카드 리스트")
            List<StoreCardDTO> storeList
    ) {}

    @Builder
    public record StoreCardDTO(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "대표 이미지", example = "https://...")
            String imageUrl,

            @Schema(description = "가게명", example = "데이브런치")
            String name,

            @Schema(description = "카테고리", example = "샌드위치&김밥")
            String category,

            @Schema(description = "최소 가격", example = "7000")
            Integer minPrice,

            @Schema(description = "최대 가격", example = "12000")
            Integer maxPrice,

            @Schema(description = "연락처", example = "051-1234-5678")
            String phoneNumber,

            @Schema(description = "별점", example = "4.7")
            Double rating,

            @Schema(description = "픽업 시간", example = "10:00 ~ 17:00")
            String pickupTimeRange
    ) {}
}