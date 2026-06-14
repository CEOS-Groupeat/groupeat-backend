package com.groupeat.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CartItemBulkAddRequest(
        @Schema(description = "장바구니에 담을 항목 리스트")
        @NotEmpty(message = "장바구니에 담을 항목이 없습니다.")
        List<@Valid CartItemAddRequest> cartItems
) {}
