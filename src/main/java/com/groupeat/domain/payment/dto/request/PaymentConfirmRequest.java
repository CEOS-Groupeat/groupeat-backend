package com.groupeat.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PaymentConfirmRequest(
        @Schema(description = "토스페이먼츠가 결제 인증 성공 후 발급한 결제 키", example = "tgen_20260528123456AbCdEf")
        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,

        @Schema(description = "주문 생성 시 서버가 발급한 Toss Payments용 주문 ID", example = "ORDER_1779722315381_5A617920")
        @NotBlank(message = "orderId는 필수입니다.")
        @Size(min = 6, max = 64, message = "orderId는 6자 이상 64자 이하이어야 합니다.")
        String orderId,

        @Schema(description = "토스페이먼츠 승인 요청 금액", example = "423700")
        @NotNull(message = "amount는 필수입니다.")
        @Positive(message = "amount는 0보다 커야 합니다.")
        Integer amount
) {
}
