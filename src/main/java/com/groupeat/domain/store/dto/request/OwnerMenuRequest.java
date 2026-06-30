package com.groupeat.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

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
        String imageUrl,

        @Schema(description = "옵션 그룹 목록")
        @Valid
        List<OptionGroupRequest> optionGroups
) {
    @Builder
    public record OptionGroupRequest(
            @Schema(description = "옵션 그룹명", example = "샌드위치 선택")
            @NotBlank(message = "옵션 그룹명은 필수입니다.")
            @Size(max = 50, message = "옵션 그룹명은 50자 이하여야 합니다.")
            String name,

            @Schema(description = "필수 선택 여부", example = "true")
            @NotNull(message = "필수 선택 여부는 필수입니다.")
            Boolean isRequired,

            @Schema(description = "다중 선택 가능 여부", example = "false")
            @NotNull(message = "다중 선택 가능 여부는 필수입니다.")
            Boolean isMultiple,

            @Schema(description = "세부 옵션 목록")
            @Valid
            List<OptionRequest> options
    ) {}

    @Builder
    public record OptionRequest(
            @Schema(description = "옵션명", example = "햄치즈")
            @NotBlank(message = "옵션명은 필수입니다.")
            @Size(max = 50, message = "옵션명은 50자 이하여야 합니다.")
            String name,

            @Schema(description = "추가 금액", example = "900")
            @NotNull(message = "추가 금액은 필수입니다.")
            @Min(value = 0, message = "추가 금액은 0 이상이어야 합니다.")
            Integer additionalPrice
    ) {}
}
