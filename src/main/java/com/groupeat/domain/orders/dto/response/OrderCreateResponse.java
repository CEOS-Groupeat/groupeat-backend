package com.groupeat.domain.orders.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record OrderCreateResponse(
        @Schema(description = "생성된 주문의 고유 식별자 (PK)", example = "10")
        Long paymentId,

        @Schema(description = "토스페이먼츠용 고유 주문 번호", example = "ORDER_20260525_0001")
        String orderId,


        @Schema(description = "실제 결제할 금액 (선결제 100% 또는 현장결제 50% 적용 가격)", example = "372400")
        Integer amount,

        @Schema(description = "고객 이름", example = "김동욱")
        String customerName
) {}