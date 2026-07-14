package com.groupeat.domain.search.dto.request;

import com.groupeat.domain.search.enums.StoreSortType;
import com.groupeat.domain.search.exception.SearchErrorStatus;
import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.global.exception.GeneralException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record StoreSearchCondition(
        @Schema(description = "텍스트 검색어", example = "데이브런치")
        String keyword,

        @Schema(description = "위치 필터", example = "마포구")
        String district,

        @Schema(description = "픽업 날짜 필터 (단일 선택)", example = "2026-04-23")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate pickupDate,

        @Schema(description = "픽업 시간 필터 (다중 선택 가능)", example = "\"12:00:00,13:00:00\"")
        @DateTimeFormat(pattern = "HH:mm:ss")
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

    // 조건이 아예 넘어오지 않았을 경우(null)를 대비한 안전한 기본 객체 생성
    public static StoreSearchCondition defaultIfNull(StoreSearchCondition condition) {
        return condition == null
                ? new StoreSearchCondition(null, null, null, null, null, null, null, null)
                : condition;
    }

    // 비즈니스 규칙 유효성 검증
    public void validate() {
        // 과거 날짜 검색 방지
        if (pickupDate != null && pickupDate.isBefore(LocalDate.now())) {
            throw new GeneralException(SearchErrorStatus.PICKUP_DATE_IN_PAST);
        }

        // 시간 조건만 입력하고 날짜는 입력하지 않은 경우 방지
        if (pickupTimes != null && !pickupTimes.isEmpty() && pickupDate == null) {
            throw new GeneralException(SearchErrorStatus.PICKUP_DATE_REQUIRED_FOR_TIME);
        }

        // 수량 및 예산 음수 방지
        if (quantity != null && quantity < 1) {
            throw new GeneralException(SearchErrorStatus.INVALID_QUANTITY);
        }
        if (budget != null && budget < 0) {
            throw new GeneralException(SearchErrorStatus.INVALID_BUDGET);
        }
    }

    // 공백만 있는 키워드는 null로 정제하여 쿼리 최적화
    public String cleanKeyword() {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}