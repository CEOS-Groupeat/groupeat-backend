package com.groupeat.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CartCalculateRequest(
        @Schema(description = "주문 할 장바구니의 CartItem ID 목록")
        @NotEmpty(message = "주문할 항목을 1개 이상 선택해야 합니다.")
        List<Long> cartItemIds
) {}