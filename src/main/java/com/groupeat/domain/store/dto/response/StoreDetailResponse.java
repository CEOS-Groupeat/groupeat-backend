package com.groupeat.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record StoreDetailResponse(

        @Schema(description = "가게 ID", example = "1")
        Long storeId,

        @Schema(description = "가게 대표 이미지", example = "https://groupeat-bucket.s3.ap-northeast-2.amazonaws.com/store1.jpg")
        String imageUrl,

        @Schema(description = "가게명", example = "데이브런치")
        String storeName,

        @Schema(description = "가게 주소", example = "마포구 00로 00길")
        String address,

        @Schema(description = "평균 별점", example = "4.7")
        Double rating,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "가게 한 줄 소개", example = "신선한 재료로 당일 제조하는 샌드위치 전문점입니다.")
        String description,

        @Schema(description = "휴무일", example = "화요일 휴무")
        String closedDays,

        @Schema(description = "주문 가능 기한", example = "3")
        Integer minOrderDays,

        @Schema(description = "할인율 기준", example = "50")
        Integer discountConditionQuantity,

        @Schema(description = "할인율", example = "5")
        Integer discountRate,

        @Schema(description = "주문 프로세스", example = "1. 예약 주문 2. 픽업 대기 3. 픽업 완료")
        String orderProcess
) {
}