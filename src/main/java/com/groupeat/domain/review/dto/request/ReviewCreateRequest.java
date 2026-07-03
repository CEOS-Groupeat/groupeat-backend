package com.groupeat.domain.review.dto.request;

import com.groupeat.domain.review.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

public record ReviewCreateRequest(

        @Schema(description = "주문 ID", example = "10")
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,

        @Schema(description = "주문한 개별 메뉴의 별점 목록")
        @NotEmpty(message = "최소 1개 이상의 메뉴 별점이 필요합니다.")
        List<MenuRatingDTO> menuRatings,

        @Schema(description = "행사 유형", example = "SEMINAR")
        @NotNull(message = "행사 유형을 선택해주세요.")
        EventType eventType,

        @Schema(description = "참여 인원", example = "56")
        @Positive(message = "참여 인원은 1명 이상이어야 합니다.")
        Integer headcount,

        @Schema(description = "1인당 예산", example = "3000")
        @Positive(message = "1인당 예산은 0원보다 커야 합니다.")
        Integer perPersonBudget,

        @Schema(description = "리뷰 내용", example = "여기 샌드위치 진짜 뚱뚱하네요!")
        @Size(max = 1000, message = "리뷰 내용은 1000자를 초과할 수 없습니다.")
        String content,

        @Schema(description = "리뷰 이미지 URL 목록")
        List<String> imageUrls
) {
    @Schema(name = "ReviewMenuRatingDTO", description = "메뉴별 별점 요청 정보")
    public record MenuRatingDTO(
            @Schema(description = "주문 항목(OrderItem) ID", example = "101")
            @NotNull(message = "주문 항목 ID는 필수입니다.")
            Long orderItemId,

            @Schema(description = "해당 메뉴 별점 (1~5)", example = "5")
            @Min(value = 1, message = "별점은 최소 1점입니다.")
            @Max(value = 5, message = "별점은 최대 5점입니다.")
            Integer rating
    ) {}
}
