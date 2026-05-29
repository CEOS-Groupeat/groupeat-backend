package com.groupeat.domain.payment.dto.response;

import com.groupeat.domain.payment.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PaymentConfirmResponse(
        @Schema(description = "결제 DB 내부 ID", example = "1")
        Long paymentId,

        @Schema(description = "Toss Payments용 주문 ID", example = "ORDER_1779722315381_5A617920")
        String orderId,

        @Schema(description = "토스페이먼츠 결제 키", example = "tgen_20260528123456AbCdEf")
        String paymentKey,

        @Schema(description = "실제 PG 승인 금액", example = "423700")
        Integer amount,

        @Schema(description = "결제 상태", example = "DONE")
        PaymentStatus status,

        @Schema(description = "결제 승인 완료 시각")
        LocalDateTime approvedAt
) {
}
