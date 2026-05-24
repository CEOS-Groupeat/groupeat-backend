package com.groupeat.domain.search.converter;

import com.groupeat.domain.search.dto.response.StoreSearchResponse;
import com.groupeat.domain.store.entity.Store;
import java.util.List;
import java.util.stream.Collectors;

public class SearchConverter {

    public static StoreSearchResponse.StoreListDTO toStoreListDTO(List<Store> stores, long totalElements) {
        List<StoreSearchResponse.StoreCardDTO> storeCards = stores.stream()
                .map(SearchConverter::toStoreCardDTO)
                .collect(Collectors.toList());

        return StoreSearchResponse.StoreListDTO.builder()
                .totalElements(totalElements)
                .storeList(storeCards)
                .build();
    }

    private static StoreSearchResponse.StoreCardDTO toStoreCardDTO(Store store) {
        String timeRange = (store.getPickupOpenTime() != null && store.getPickupCloseTime() != null)
                ? store.getPickupOpenTime() + " ~ " + store.getPickupCloseTime()
                : "시간 정보 없음";

        return StoreSearchResponse.StoreCardDTO.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .name(store.getStoreName())
                .category(store.getCategory() != null ? store.getCategory().getDescription() : null)
                .minPrice(store.getMinPrice())
                .maxPrice(store.getMaxPrice())
                .phoneNumber(store.getPhoneNumber())
                .rating(store.getReviewRating())
                .pickupTimeRange(timeRange)
                .build();
    }
}