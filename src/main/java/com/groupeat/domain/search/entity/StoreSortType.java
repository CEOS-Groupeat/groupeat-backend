package com.groupeat.domain.search.entity;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum StoreSortType {
    NONE("전체"),
    DISCOUNT_DESC("할인율 높은 순"),
    PRICE_ASC("가격 낮은 순"),
    PRICE_DESC("가격 높은 순"),
    ORDER_DESC("주문 많은 순"),
    RATING_DESC("별점 높은 순");

    private final String description;
}