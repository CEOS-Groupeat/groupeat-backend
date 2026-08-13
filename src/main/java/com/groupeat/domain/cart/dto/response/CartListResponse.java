package com.groupeat.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record CartListResponse(
        @Schema(description = "장바구니에 담긴 메뉴 종류(항목) 개수", example = "3")
        Integer totalItemCount,

        @Schema(description = "장바구니 픽업 날짜", example = "2026-07-02")
        LocalDate pickupDate,

        @Schema(description = "장바구니 픽업 시간", example = "14:30:00")
        LocalTime pickupTime,

        @Schema(description = "가게별로 그룹화된 장바구니 목록")
        List<StoreCartDTO> storeCarts
) {
    @Builder
    public record StoreCartDTO(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게명", example = "데이브런치")
            String storeName,

            @Schema(description = "가게 카테고리", example = "샌드위치&김밥")
            String storeCategory,

            @Schema(description = "해당 가게에 담긴 메뉴 목록")
            List<CartItemDTO> cartItems,

            @Schema(description = "해당 가게의 주문 총 금액 (단순 합산)", example = "744800")
            Integer storeTotalPrice
    ) {}

    @Builder
    public record CartItemDTO(
            @Schema(description = "장바구니 항목 ID", example = "100")
            Long cartItemId,

            @Schema(description = "메뉴명", example = "반반 세트 1")
            String menuName,

            @Schema(description = "선택한 옵션명 목록", example = "[\"햄치즈 샌드위치\", \"참치 김밥\"]")
            List<String> optionNames,

            @Schema(description = "메뉴 이미지", example = "https://...")
            String imageUrl,

            @Schema(description = "수량", example = "56")
            Integer quantity,

            @Schema(description = "1인당 금액 (메뉴가 + 옵션가)", example = "7000")
            Integer unitPrice,

            @Schema(description = "항목별 할인 금액", example = "19600")
            Integer discountAmount,

            @Schema(description = "적용된 할인율 (%)", example = "5")
            Integer discountRate,

            @Schema(description = "총 금액 (할인 적용된 최종가)", example = "372400")
            Integer finalPrice
    ) {}
}
