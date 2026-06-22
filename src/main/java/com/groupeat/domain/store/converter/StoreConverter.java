package com.groupeat.domain.store.converter;

import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.entity.Store;

public class StoreConverter {

    public static StoreDetailResponse toStoreDetailResponse(Store store) {

        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .reviewRating(store.getReviewRating())
                .reviewCount(store.getReviewCount())
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .closedDays(store.getClosedDays())
                .pickupOpenTime(store.getPickupOpenTime())
                .pickupCloseTime(store.getPickupCloseTime())
                .minOrderDays(store.getMinOrderDays())
                .discountConditionQuantity(store.getDiscountConditionQuantity())
                .discountRate(store.getDiscountRate())
                .orderProcess(store.getOrderProcess())
                .build();
    }

    public static OwnerStoreResponse toOwnerStoreResponse(Store store) {

        return OwnerStoreResponse.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .storeName(store.getStoreName())
                .location(toLocationDTO(store))
                .category(store.getCategory())
                .categoryName(store.getCategory() != null ? store.getCategory().getDescription() : null)
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .discount(toDiscountDTO(store))
                .build();
    }

    private static OwnerStoreResponse.LocationDTO toLocationDTO(Store store) {
        return OwnerStoreResponse.LocationDTO.builder()
                .address(store.getAddress())
                .district(store.getDistrict() != null ? store.getDistrict() : getRegionDescription(store))
                .neighborhood(store.getNeighborhood())
                .detailAddress(store.getDetailAddress())
                .build();
    }

    private static OwnerStoreResponse.DiscountDTO toDiscountDTO(Store store) {
        return OwnerStoreResponse.DiscountDTO.builder()
                .conditionQuantity(store.getDiscountConditionQuantity())
                .rate(store.getDiscountRate())
                .build();
    }

    private static String getRegionDescription(Store store) {
        return store.getRegion() != null ? store.getRegion().getDescription() : null;
    }
}
