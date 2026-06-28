package com.groupeat.domain.store.dto.response;

import com.groupeat.domain.store.enums.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record OwnerStoreResponse(

        @Schema(description = "가게 ID", example = "1")
        Long storeId,

        @Schema(description = "가게 대표 이미지", example = "https://groupeat-bucket.s3.ap-northeast-2.amazonaws.com/store1.jpg")
        String imageUrl,

        @Schema(description = "가게명", example = "데이브런치")
        String storeName,

        @Schema(description = "가게 위치")
        LocationDTO location,

        @Schema(description = "가게 카테고리", example = "SANDWICH_KIMBAP")
        StoreCategory category,

        @Schema(description = "가게 카테고리명", example = "샌드위치&김밥")
        String categoryName,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "가게 한 줄 소개", example = "신선한 재료로 당일 제조하는 샌드위치 전문점입니다.")
        String description,

        @Schema(description = "할인 정보")
        DiscountDTO discount
) {

    @Builder
    public record LocationDTO(
            @Schema(description = "도로명/지번 주소", example = "서울특별시 마포구 00로 00길 12")
            String address,

            @Schema(description = "구", example = "마포구")
            String district,

            @Schema(description = "동", example = "서교동")
            String neighborhood,

            @Schema(description = "상세주소", example = "00로 00길 12, 3층")
            String detailAddress
    ) {
    }

    @Builder
    public record DiscountDTO(
            @Schema(description = "할인 조건 수량", example = "50")
            Integer conditionQuantity,

            @Schema(description = "할인율", example = "5")
            Integer rate
    ) {
    }
}
