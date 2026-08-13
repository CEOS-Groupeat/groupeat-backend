package com.groupeat.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record MenuListResponse(
        @Schema(description = "메뉴 목록")
        List<MenuDetailDTO> menus
) {
    @Builder
    public record MenuDetailDTO(
            @Schema(description = "메뉴 ID", example = "1")
            Long menuId,
            @Schema(description = "메뉴명", example = "반반 세트")
            String name,
            @Schema(description = "기본 가격", example = "7800")
            Integer basePrice,
            @Schema(description = "메뉴 설명", example = "샌드위치 반 + 김밥 반")
            String description,
            @Schema(description = "메뉴 이미지", example = "https://...")
            String imageUrl,
            @Schema(description = "옵션 그룹 목록")
            List<OptionGroupDTO> optionGroups
    ) {}

    @Builder
    public record OptionGroupDTO(
            @Schema(description = "옵션 그룹 ID", example = "1")
            Long optionGroupId,
            @Schema(description = "옵션 그룹명", example = "샌드위치 선택 / 김밥 선택")
            String name,
            @Schema(description = "필수 선택 여부", example = "true")
            Boolean isRequired,
            @Schema(description = "다중 선택 가능 여부", example = "false")
            Boolean isMultiple,
            @Schema(description = "세부 옵션 목록")
            List<OptionDTO> options
    ) {}

    @Builder
    public record OptionDTO(
            @Schema(description = "세부 옵션 ID", example = "1")
            Long optionId,
            @Schema(description = "옵션명", example = "햄치즈")
            String name,
            @Schema(description = "추가 금액", example = "900")
            Integer additionalPrice
    ) {}
}