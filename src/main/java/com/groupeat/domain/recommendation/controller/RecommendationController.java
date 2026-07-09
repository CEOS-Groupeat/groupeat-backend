package com.groupeat.domain.recommendation.controller;

import com.groupeat.domain.recommendation.dto.response.RecommendationListResponse;
import com.groupeat.domain.recommendation.service.RecommendationService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recommendation", description = "메인 화면 가게 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "인기 만점 가게 추천 조회", description = "별점이 높은 가게 2개를 추천하여 반환합니다.")
    @GetMapping("/top-rated")
    public ApiResponse<RecommendationListResponse> getTopRatedStores() {
        RecommendationListResponse response = recommendationService.getTopRatedStores();
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "할인율 높은 가게 추천 조회", description = "할인율이 높은 가게 2개를 추천하여 반환합니다.")
    @GetMapping("/high-discount")
    public ApiResponse<RecommendationListResponse> getHighDiscountStores() {
        RecommendationListResponse response = recommendationService.getHighDiscountStores();
        return ApiResponse.onSuccess(response);
    }
}