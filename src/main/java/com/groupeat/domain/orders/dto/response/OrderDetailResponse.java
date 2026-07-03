package com.groupeat.domain.orders.dto.response;

import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.PaymentMethod;
import com.groupeat.domain.payment.enums.PaymentProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class OrderDetailResponse {
    @Builder
    public record OrderDetailDTO(
            @Schema(description = "가게 이름")
            String storeName,

            @Schema(description = "픽업 날짜", example = "2026-06-25")
            LocalDate pickupDate,

            @Schema(description = "픽업 시간", example = "14:30")
            LocalTime pickupTime,

            @Schema(description = "주문자 정보")
            OrdererInfoDTO ordererInfo,

            @Schema(description = "주문 상품 정보 목록")
            List<OrderMenuDTO> orderMenus,

            @Schema(description = "결제 정보")
            PaymentInfoDTO paymentInfo,

            @Schema(description = "주문 상태", example = "PENDING")
            OrderStatus orderStatus
    ) {}

    @Builder
    public record OrdererInfoDTO(
            @Schema(description = "주문자명", example = "김동욱")
            String customerName,

            @Schema(description = "주문 단체명", example = "CEOS 데모데이")
            String groupName,

            @Schema(description = "연락처", example = "010-1234-5678")
            String phoneNumber,

            @Schema(description = "주문 일자", example = "2026-06-20")
            LocalDate orderDate,

            @Schema(description = "주문 시간", example = "18:30")
            LocalTime orderTime,

            @Schema(description = "요청사항", example = "픽업 시간에 맞춰서 준비해 주세요.")
            String requests
    ) {}

    @Builder
    public record OrderMenuDTO(
            @Schema(description = "주문 메뉴 및 옵션", example = "반반 세트")
            String menuName,

            @Schema(description = "선택한 옵션 목록")
            List<OrderMenuOptionDTO> options,

            @Schema(description = "주문 수량", example = "2")
            Integer quantity,

            @Schema(description = "메뉴 이미지 URL", example = "https://image.url/menu.png")
            String menuImageUrl,

            @Schema(description = "할인율(%)", example = "10")
            Integer discountRate,

            @Schema(description = "해당 메뉴 총 금액", example = "30000")
            Integer totalAmount
    ) {}

    @Builder
    public record OrderMenuOptionDTO(
            @Schema(description = "옵션명", example = "햄치즈 샌드위치")
            String optionName
    ) {}

    @Builder
    public record PaymentInfoDTO(
            @Schema(description = "결제 방식", example = "PREPAID")
            PaymentMethod paymentMethod,

            @Schema(description = "결제 수단", example = "토스페이")
            PaymentProvider paymentMeans,

            @Schema(description = "1인당 금액(소수점 삭제)", example = "7000")
            Integer perPersonAmount,

            @Schema(description = "총 할인율(%)", example = "10")
            Integer discountRate,

            @Schema(description = "총 할인 금액", example = "5000")
            Integer totalDiscountAmount,

            @Schema(description = "할인 전 총 금액", example = "35000")
            Integer originalTotalAmount,

            @Schema(description = "최종 결제 금액", example = "30000")
            Integer finalPaymentAmount
    ) {}
}
