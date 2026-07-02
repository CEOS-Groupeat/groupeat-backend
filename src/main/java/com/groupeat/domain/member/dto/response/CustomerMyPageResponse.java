package com.groupeat.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerMyPageResponse(
        @Schema(description = "회원의 전체 주문 수", example = "12")
        long orderCount,

        @Schema(description = "리뷰 수. 리뷰 기능 구현 전까지 0 반환", example = "0")
        long reviewCount,

        @Schema(description = "즐겨찾기 수. 즐겨찾기 기능 구현 전까지 0 반환", example = "0")
        long favoriteCount
) {
    public static CustomerMyPageResponse of(long orderCount) {
        return new CustomerMyPageResponse(orderCount, 0, 0);
    }
}
