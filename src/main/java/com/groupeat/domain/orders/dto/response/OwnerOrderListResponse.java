package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.PaymentMethod;
import com.groupeat.domain.orders.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class OwnerOrderListResponse {

    @Builder
    public record OwnerOrderListDTO(
            @Schema(description = "조건에 해당하는 주문 개수", example = "5")
            long totalElements,

            @Schema(description = "주문 카드 리스트")
            List<OrderCardDTO> orderList,

            @Schema(description = "다음 페이지 존재 여부 (무한 스크롤용)", example = "true")
            boolean hasNext,

            @Schema(description = "다음 커서 ID (마지막 주문의 PK ID)", example = "42")
            Long nextCursor
    ) {}

    @Builder
    public record OrderCardDTO(
            @Schema(description = "주문 ID (PK)", example = "12")
            Long orderId,

            @Schema(description = "주문 상태", example = "PAID")
            OrderStatus orderStatus,

            @Schema(description = "주문자명", example = "김동욱")
            String customerName,

            @Schema(description = "주문 단체명", example = "CEOS 데모데이")
            String groupName,

            @Schema(description = "픽업 날짜", example = "2026-06-21")
            LocalDate pickupDate,

            @Schema(description = "픽업 시간", example = "13:00:00")
            LocalTime pickupTime,

            @Schema(description = "총 금액 (할인 적용된 최종 금액)", example = "35000")
            Integer totalAmount,

            @Schema(description = "주문 상품 리스트 (메뉴명과 수량 분리)")
            List<OrderCardItemDTO> items,

            @Schema(description = "재주문 여부", example = "false")
            Boolean isReorder,

            @Schema(description = "결제 방식 (확정 탭에서만 노출, 그 외엔 null)", example = "PREPAID")
            PaymentMethod paymentMethod
    ) {}

    @Builder
    public record OrderCardItemDTO(
            @Schema(description = "주문 메뉴명", example = "반반 세트")
            String menuName,

            @Schema(description = "주문 수량", example = "2")
            Integer quantity
    ) {}
}