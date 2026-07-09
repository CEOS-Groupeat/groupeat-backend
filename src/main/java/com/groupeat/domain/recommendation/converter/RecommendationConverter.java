package com.groupeat.domain.recommendation.converter;

import com.groupeat.domain.recommendation.dto.response.RecommendationResponse;
import com.groupeat.domain.recommendation.dto.response.RecommendationResponse.RecommendationCardDTO;
import com.groupeat.domain.store.entity.Store;

import java.util.List;

public class RecommendationConverter {

    public static RecommendationResponse toRecommendationResponse(
            List<Store> topRatedStores,
            List<Store> highDiscountStores
    ) {
        return RecommendationResponse.builder()
                .topRatedStores(topRatedStores.stream()
                        .map(RecommendationConverter::toCardDTO)
                        .toList())
                .highDiscountStores(highDiscountStores.stream()
                        .map(RecommendationConverter::toCardDTO)
                        .toList())
                .build();
    }

    private static RecommendationCardDTO toCardDTO(Store store) {
        return RecommendationCardDTO.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .category(store.getCategory() != null ? store.getCategory().getDescription() : null)
                .storeName(store.getStoreName())
                .district(store.getDistrict())
                .neighborhood(store.getNeighborhood())
                .minPrice(store.getMinPrice())
                .maxPrice(store.getMaxPrice())
                .rating(store.getReviewRating() != null ? Math.round(store.getReviewRating() * 10) / 10.0 : 0.0)
                .reviewCount(store.getReviewCount())
                .build();
    }
}
