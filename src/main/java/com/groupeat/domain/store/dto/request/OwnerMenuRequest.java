package com.groupeat.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OwnerMenuRequest(

        @Schema(description = "메뉴명", example = "햄치즈 샌드위치")
        @NotBlank(message = "메뉴명은 필수입니다.")
        @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다.")
        String name,

        @Schema(description = "기본 가격", example = "7800")
        @NotNull(message = "기본 가격은 필수입니다.")
        @Min(value = 0, message = "기본 가격은 0 이상이어야 합니다.")
        Integer basePrice,

        @Schema(description = "메뉴 설명", example = "햄과 치즈가 들어간 샌드위치입니다.")
        @Size(max = 200, message = "메뉴 설명은 200자 이하여야 합니다.")
        String description,

        @Schema(description = "메뉴 이미지 URL", example = "https://groupeat-bucket.s3.ap-northeast-2.amazonaws.com/menus/1.jpg")
        String imageUrl
) {
}
