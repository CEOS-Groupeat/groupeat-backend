package com.groupeat.domain.search.cursor;

import com.groupeat.domain.search.enums.StoreSortType;

public record StoreSearchCursor(int version, StoreSortType sortType, long storeId,
                                String value, String conditionKey) {
    public Object sortValue() {
        if (value == null) return null;
        return switch (sortType) {
            case NONE -> value;
            case DISCOUNT_DESC, PRICE_ASC, PRICE_DESC -> Integer.valueOf(value);
            case RATING_DESC -> Double.valueOf(value);
            case ORDER_DESC -> Long.valueOf(value);
        };
    }
}
