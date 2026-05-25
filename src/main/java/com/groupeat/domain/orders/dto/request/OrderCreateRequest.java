package com.groupeat.domain.orders.dto.request;

import com.groupeat.domain.orders.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrderCreateRequest(
        @Schema(description = "주문할 장바구니 항목 ID 목록", example = "[1, 2]")
        @NotEmpty(message = "주문할 항목을 1개 이상 선택해야 합니다.")
        List<Long> cartItemIds,

        @Schema(description = "주문자명", example = "김동욱")
        @NotBlank(message = "주문자명은 필수 입력 항목입니다.")
        String customerName,

        @Schema(description = "연락처", example = "010-1234-5678")
        @NotBlank(message = "연락처는 필수 입력 항목입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String customerPhone,

        @Schema(description = "단체명 / 행사명 (선택)", example = "CEOS 데모데이")
        String groupName,

        @Schema(description = "요청사항 (선택)", example = "맛있게 만들어 주세요.")
        String requests,

        @Schema(description = "결제 방식 (PREPAID: 선결제, ON_SITE: 현장결제)", example = "PREPAID")
        @NotNull(message = "결제 방식 선택은 필수입니다.")
        PaymentMethod paymentMethod
) {}