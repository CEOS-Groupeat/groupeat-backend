package com.groupeat.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerMyPageResponse(
        @Schema(description = "회원의 전체 주문 수", example = "12")
        long orderCount,

        @Schema(description = "회원이 작성한 리뷰 수", example = "3")
        long reviewCount
) {
    public static CustomerMyPageResponse of(long orderCount, long reviewCount) {
        return new CustomerMyPageResponse(orderCount, reviewCount);
    }
}
