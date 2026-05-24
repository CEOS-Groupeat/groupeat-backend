package com.groupeat.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CartItemAddRequest(
        @Schema(description = "가게 ID", example = "1")
        @NotNull(message = "가게 ID는 필수입니다.")
        Long storeId,

        @Schema(description = "메뉴 ID", example = "1")
        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @Schema(description = "수량", example = "56")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @Schema(description = "선택한 옵션 ID 목록 (없으면 빈 리스트 또는 null)")
        List<Long> optionIds
) {}