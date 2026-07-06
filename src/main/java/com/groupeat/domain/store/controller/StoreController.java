package com.groupeat.domain.store.controller;

import com.groupeat.domain.review.dto.response.ReviewListResponse;
import com.groupeat.domain.review.service.ReviewService;
import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.dto.response.PickupTimeResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.service.MenuService;
import com.groupeat.domain.store.service.StoreService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Store", description = "가게 상세 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;
    private final MenuService menuService;
    private final ReviewService reviewService;

    @Operation(summary = "가게 상세 정보 조회", description = "가게 ID를 통해 가게 상세 정보를 조회합니다.")
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> getStoreDetail(
            @PathVariable Long storeId,
            @Nullable @AuthenticationPrincipal Long userId
    ) {
        StoreDetailResponse result = storeService.getStoreInfo(storeId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "가게 메뉴 목록 조회", description = "특정 가게의 전체 메뉴와 세부 옵션들을 조회합니다.")
    @GetMapping("/{storeId}/menus")
    public ApiResponse<MenuListResponse> getStoreMenus(
            @PathVariable Long storeId,
            @Nullable @AuthenticationPrincipal Long userId
    ) {
        MenuListResponse result = menuService.getStoreMenus(storeId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "특정 날짜의 픽업 가능 시간 및 수량 조회", description = "날짜를 선택하면 해당 날짜의 30분 단위 시간대별 잔여 수량을 반환합니다.")
    @GetMapping("/{storeId}/pickup-times")
    public ApiResponse<PickupTimeResponse> getPickupTimes(
            @PathVariable Long storeId,
            @RequestParam LocalDate date
    ) {
        PickupTimeResponse result = storeService.getAvailablePickupTimes(storeId, date);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{storeId}/reviews")
    @Operation(summary = "가게 리뷰 목록 조회", description = "특정 가게에 작성된 리뷰 목록을 최신순으로 조회합니다.")
    public ApiResponse<ReviewListResponse> getStoreReviews(
            @PathVariable Long storeId,
            @RequestParam(required = false) @Schema(description = "마지막으로 조회된 리뷰 ID (첫 페이지는 null)") Long lastReviewId,
            @RequestParam(defaultValue = "10") @Schema(description = "조회할 개수") int size
    ) {
        ReviewListResponse response = reviewService.getStoreReviews(storeId, lastReviewId, size);
        return ApiResponse.onSuccess(response);
    }
}
