package com.groupeat.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CartCalculateResponse(
        @Schema(description = "계산 대상 가게 ID", example = "1")
        Long storeId,

        @Schema(description = "총 주문 수량", example = "112")
        Integer totalQuantity,

        @Schema(description = "할인 전 원가 총액", example = "784000")
        Integer totalOriginalPrice,

        @Schema(description = "적용된 총 할인 금액", example = "39200")
        Integer totalDiscountAmount,

        @Schema(description = "최종 결제 예정 금액 (원가 - 할인액)", example = "744800")
        Integer finalPaymentAmount,

        @Schema(description = "계산에 포함된 메뉴들의 상세 정보")
        List<CalculatedItem> calculatedItems
) {
    // 🌟 이 부분이 핵심! UI 텍스트(menuSummary) 없이 순수 '계산된 숫자'만 담는 내부 레코드
    public record CalculatedItem(
            @Schema(description = "장바구니 항목 ID")
            Long cartItemId,

            @Schema(description = "수량")
            Integer quantity,

            @Schema(description = "1인당 단가 (메뉴가 + 옵션가)")
            Integer unitPrice,

            @Schema(description = "해당 항목 원가 총액")
            Integer itemOriginalPrice,

            @Schema(description = "해당 항목 할인 금액")
            Integer itemDiscountAmount,

            @Schema(description = "해당 항목 최종 금액")
            Integer itemFinalPrice
    ) {}
}