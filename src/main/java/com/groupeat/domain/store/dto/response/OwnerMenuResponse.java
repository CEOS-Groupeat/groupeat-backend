package com.groupeat.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record OwnerMenuResponse(

        @Schema(description = "메뉴 ID", example = "1")
        Long menuId,

        @Schema(description = "메뉴명", example = "햄치즈 샌드위치")
        String name,

        @Schema(description = "기본 가격", example = "7800")
        Integer basePrice,

        @Schema(description = "메뉴 설명", example = "햄과 치즈가 들어간 샌드위치입니다.")
        String description,

        @Schema(description = "메뉴 이미지 URL", example = "https://groupeat-bucket.s3.ap-northeast-2.amazonaws.com/menus/1.jpg")
        String imageUrl
) {
}
