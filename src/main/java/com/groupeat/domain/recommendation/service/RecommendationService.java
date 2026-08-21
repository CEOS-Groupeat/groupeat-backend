package com.groupeat.domain.recommendation.service;

import com.groupeat.domain.recommendation.converter.RecommendationConverter;
import com.groupeat.domain.recommendation.dto.response.RecommendationListResponse;
import com.groupeat.domain.recommendation.repository.RecommendationRepository;
import com.groupeat.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private static final int RECOMMENDATION_LIMIT = 2;

    @Cacheable(cacheNames = "topRatedStores")
    public RecommendationListResponse getTopRatedStores() {
        List<Store> stores = recommendationRepository.findTopRatedStores(RECOMMENDATION_LIMIT);
        return RecommendationConverter.toRecommendationListResponse(stores);
    }

    @Cacheable(cacheNames = "highDiscountStores")
    public RecommendationListResponse getHighDiscountStores() {
        List<Store> stores = recommendationRepository.findHighDiscountStores(RECOMMENDATION_LIMIT);
        return RecommendationConverter.toRecommendationListResponse(stores);
    }
}