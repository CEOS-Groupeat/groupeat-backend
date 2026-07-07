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
            @Schema(description = "조건에 해당하는 조회 목록 개수", example = "5")
            long totalElements,

            @Schema(description = "주문 카드 리스트")
            List<OrderCardDTO> orderList,

            @Schema(description = "다음 페이지 존재 여부 (무한 스크롤용)", example = "true")
            boolean hasNext,

            @Schema(description = "다음 커서 ID (마지막 주문의 PK ID)", example = "42")
            Long nextCursor
) {
    @Builder
    @Schema(name = "CustomerOrderCardDTO", description = "고객용 주문 카드 응답")
    public record OrderCardDTO(
            @Schema(description = "주문 고유 ID", example = "1")
            Long orderId,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게명", example = "데이브런치")
            String storeName,

            @Schema(description = "가게 대표 이미지", example = "https://...")
            String storeImageUrl,

            @Schema(description = "픽업 예정 날짜", example = "2026-04-23")
            LocalDate pickupDate,

            @Schema(description = "픽업 예정 시간", example = "10:00:00")
            LocalTime pickupTime,

            @Schema(description = "주문 메뉴 요약명", example = "반반 세트 외 1개")
            String menuSummary,

            @Schema(description = "주문 상태", example = "PENDING")
            OrderStatus orderStatus,

            @Schema(description = "리뷰 작성 여부", example = "false")
            boolean hasReview
    ) {}
}