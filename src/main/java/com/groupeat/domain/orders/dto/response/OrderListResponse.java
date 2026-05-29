package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record OrderListResponse(
        @Schema(description = "주문 내역 리스트")
        List<OrderCardDTO> orders,

        @Schema(description = "전체 주문 개수", example = "5")
        long totalElements,

        @Schema(description = "다음 페이지 존재 여부 (무한 스크롤용)", example = "true")
        boolean hasNext
) {
    @Builder
    public record OrderCardDTO(
            @Schema(description = "주문 고유 ID", example = "1")
            Long orderId,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게명", example = "데이브런치")
            String storeName,

            @Schema(description = "가게 대표 이미지", example = "https://...")
            String storeImageUrl,

            @Schema(description = "주문 생성 날짜", example = "2026-04-20")
            LocalDate orderDate,

            @Schema(description = "주문 생성 시간", example = "22:30:00")
            LocalTime orderTime,

            @Schema(description = "픽업 예정 날짜", example = "2026-04-23")
            LocalDate pickupDate,

            @Schema(description = "픽업 예정 시간", example = "10:00:00")
            LocalTime pickupTime,

            @Schema(description = "주문 메뉴 요약명", example = "반반 세트 외 1개")
            String menuSummary,

            @Schema(description = "할인 전 원가", example = "392000")
            Integer totalOriginalPrice,

            @Schema(description = "최종 결제 금액", example = "372400")
            Integer paymentAmount,

            @Schema(description = "주문 상태", example = "PENDING")
            OrderStatus orderStatus,

            @Schema(description = "결제 방식 (선결제/현장결제)", example = "PREPAID")
            PaymentMethod paymentMethod
    ) {}
}