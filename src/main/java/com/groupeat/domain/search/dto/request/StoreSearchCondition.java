package com.groupeat.domain.search.dto.request;

import com.groupeat.domain.search.enums.StoreSortType;
import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record StoreSearchCondition(
        @Schema(description = "텍스트 검색어", example = "데이브런치")
        String keyword,

        @Schema(description = "위치 필터", example = "마포구")
        StoreRegion region,

        @Schema(description = "픽업 날짜 필터 (단일 선택)", example = "2026-04-23")
        LocalDate pickupDate,

        @Schema(description = "픽업 시간 필터 (다중 선택 가능)", example = "[\"12:00:00\", \"13:00:00\"]")
        List<LocalTime> pickupTimes,

        @Schema(description = "주문 수량 필터", example = "50")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @Schema(description = "1인당 예산 필터", example = "10000")
        Integer budget,

        @Schema(description = "카테고리 필터", example = "샌드위치&김밥")
        StoreCategory category,

        @Schema(description = "정렬 기준(기본값: NONE - 가나다 순)", example = "NONE")
        StoreSortType sortType
) {
    public StoreSortType sortType() {
        return this.sortType == null ? StoreSortType.NONE : this.sortType;
    }
}