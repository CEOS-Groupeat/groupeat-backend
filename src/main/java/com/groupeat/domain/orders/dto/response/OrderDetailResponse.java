package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record OrderDetailResponse(
        @Schema(description = "주문 고유 ID)", example = "1")
        Long orderId,

        @Schema(description = "주문 상태", example = "PENDING")
        OrderStatus orderStatus,

        @Schema(description = "주문자", example = "김동욱")
        String customerName,

        @Schema(description = "연락처", example = "010-1234-5678")
        String customerPhone,

        @Schema(description = "픽업 날짜", example = "2026-04-23")
        LocalDate pickupDate,

        @Schema(description = "픽업 시간", example = "10:00:00")
        LocalTime pickupTime,

        @Schema(description = "주문 메뉴 및 수량 목록")
        List<OrderDetailItemDTO> items,

        @Schema(description = "총 금액 (할인 적용된 최종 결제액)", example = "372400")
        Integer paymentAmount,

        @Schema(description = "결제 상태", example = "ON_SITE")
        PaymentMethod paymentMethod,

        @Schema(description = "주문 날짜", example = "2026-04-20")
        LocalDate orderDate,

        @Schema(description = "주문 시간", example = "17:30:00")
        LocalTime orderTime
) {
    @Builder
    public record OrderDetailItemDTO(
            @Schema(description = "메뉴명", example = "반반 세트")
            String menuName,

            @Schema(description = "수량", example = "56")
            Integer quantity
    ) {}
}