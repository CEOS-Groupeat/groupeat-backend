package com.groupeat.domain.member.dto.response;

public record CustomerMyPageResponse(
        long orderCount,
        long reviewCount,
        long favoriteCount
) {
    public static CustomerMyPageResponse of(long orderCount) {
        return new CustomerMyPageResponse(orderCount, 0, 0);
    }
}
