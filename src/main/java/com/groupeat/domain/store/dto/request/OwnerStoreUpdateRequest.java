package com.groupeat.domain.store.dto.request;

import com.groupeat.domain.store.enums.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OwnerStoreUpdateRequest(

        @Schema(description = "가게 대표 이미지 URL", example = "https://groupeat-bucket.s3.ap-northeast-2.amazonaws.com/stores/1/main.jpg")
        String imageUrl,

        @Schema(description = "가게명", example = "데이브런치")
        @NotBlank(message = "가게명은 필수입니다.")
        @Size(max = 100, message = "가게명은 100자 이하여야 합니다.")
        String storeName,

        @Schema(description = "가게 위치")
        @Valid
        @NotNull(message = "가게 위치는 필수입니다.")
        LocationDTO location,

        @Schema(description = "가게 카테고리", example = "SANDWICH_KIMBAP")
        @NotNull(message = "가게 카테고리는 필수입니다.")
        StoreCategory category,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String phoneNumber,

        @Schema(description = "가게 한 줄 소개", example = "신선한 재료로 당일 제조하는 샌드위치 전문점입니다.")
        @Size(max = 100, message = "가게 한 줄 소개는 100자 이하여야 합니다.")
        String description,

        @Schema(description = "할인 정보")
        @Valid
        DiscountDTO discount
) {

    @Builder
    public record LocationDTO(
            @Schema(description = "도로명/지번 주소", example = "서울특별시 마포구 00로 00길 12")
            @NotBlank(message = "주소는 필수입니다.")
            String address,

            @Schema(description = "구", example = "마포구")
            @NotBlank(message = "구 정보는 필수입니다.")
            @Size(max = 50, message = "구 정보는 50자 이하여야 합니다.")
            String district,

            @Schema(description = "동", example = "서교동")
            @Size(max = 50, message = "동 정보는 50자 이하여야 합니다.")
            String neighborhood,

            @Schema(description = "상세주소", example = "3층")
            String detailAddress
    ) {
    }

    @Builder
    public record DiscountDTO(
            @Schema(description = "할인 조건 수량", example = "50")
            @Min(value = 1, message = "할인 조건 수량은 1 이상이어야 합니다.")
            Integer conditionQuantity,

            @Schema(description = "할인율", example = "5")
            @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
            @Max(value = 100, message = "할인율은 100 이하여야 합니다.")
            Integer rate
    ) {
    }
}
