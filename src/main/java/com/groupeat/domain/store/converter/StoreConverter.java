package com.groupeat.domain.store.converter;

import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.entity.Store;

public class StoreConverter {

    public static StoreDetailResponse toStoreDetailResponse(Store store) {

        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .rating(store.getRating())
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .closedDays(store.getClosedDays())
                .minOrderDays(store.getMinOrderDays())
                .discountConditionQuantity(store.getDiscountConditionQuantity())
                .discountRate(store.getDiscountRate())
                .orderProcess(store.getOrderProcess())
                .build();
    }
}
