package com.groupeat.domain.search.converter;

import com.groupeat.domain.search.dto.response.StoreSearchResponse;
import com.groupeat.domain.store.entity.Store;

import java.util.List;
import java.util.stream.Collectors;

public class SearchConverter {

    public static StoreSearchResponse.StoreListDTO toStoreListDTO(
            List<Store> stores,
            long totalElements
    ) {
        List<StoreSearchResponse.StoreCardDTO> storeCards = stores.stream()
                .map(SearchConverter::toStoreCardDTO)
                .collect(Collectors.toList());

        return StoreSearchResponse.StoreListDTO.builder()
                .totalElements(totalElements)
                .storeList(storeCards)
                .build();
    }

    private static StoreSearchResponse.StoreCardDTO toStoreCardDTO(Store store) {
        return StoreSearchResponse.StoreCardDTO.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .name(store.getStoreName())
                .category(store.getCategory() != null ? store.getCategory().getDescription() : null)
                .minPrice(store.getMinPrice())
                .maxPrice(store.getMaxPrice())
                .rating(store.getReviewRating())
                .reviewCount(store.getReviewCount())
                .district(store.getDistrict() != null ? store.getDistrict() : null)
                .neighborhood(store.getNeighborhood())
                .build();
    }
}
