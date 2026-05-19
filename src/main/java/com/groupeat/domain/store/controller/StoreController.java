package com.groupeat.domain.store.controller;

import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.service.MenuService;
import com.groupeat.domain.store.service.StoreService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가게 정보 API", description = "가게 상세 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;
    private final MenuService menuService;

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
}
