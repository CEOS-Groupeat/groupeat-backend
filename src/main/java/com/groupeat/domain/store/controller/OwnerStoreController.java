package com.groupeat.domain.store.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.store.dto.request.OwnerMenuRequest;
import com.groupeat.domain.store.dto.request.OwnerStoreOrderScheduleRequest;
import com.groupeat.domain.store.dto.request.OwnerStoreUpdateRequest;
import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.dto.response.OwnerMenuResponse;
import com.groupeat.domain.store.dto.response.OwnerStoreOrderScheduleResponse;
import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.dto.response.OwnerStoreUpsertResult;
import com.groupeat.domain.store.service.OwnerMenuService;
import com.groupeat.domain.store.service.OwnerStoreOrderScheduleService;
import com.groupeat.domain.store.service.OwnerStoreService;
import com.groupeat.global.apiPayload.ApiResponse;
import com.groupeat.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Owner Store", description = "사업자 가게 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/store")
public class OwnerStoreController {

    private final OwnerStoreService ownerStoreService;
    private final OwnerMenuService ownerMenuService;
    private final OwnerStoreOrderScheduleService ownerStoreOrderScheduleService;

    @Operation(summary = "내 가게 조회", description = "로그인한 사업자 회원의 가게 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<OwnerStoreResponse> getMyStore(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        OwnerStoreResponse result = ownerStoreService.getMyStore(member);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "내 가게 저장", description = "가게가 없으면 생성하고, 있으면 수정합니다.")
    @PutMapping
    public ResponseEntity<ApiResponse<OwnerStoreResponse>> upsertMyStore(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody OwnerStoreUpdateRequest request
    ) {
        OwnerStoreUpsertResult result = ownerStoreService.upsertMyStore(member, request);
        SuccessStatus status = result.created() ? SuccessStatus.CREATED : SuccessStatus.OK;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ApiResponse.of(status, result.store()));
    }

    @Operation(summary = "내 가게 주문 가능 일정 조회", description = "로그인한 사업자 회원의 가게 주문 가능 일정 설정을 조회합니다.")
    @GetMapping("/order-schedule")
    public ApiResponse<OwnerStoreOrderScheduleResponse> getMyStoreOrderSchedule(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        OwnerStoreOrderScheduleResponse result = ownerStoreOrderScheduleService.getMyOrderSchedule(member);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "내 가게 주문 가능 일정 전체 저장",
            description = "운영정보의 최종 상태를 전체 저장합니다. "
                    + "공통 설정과 MONDAY~SUNDAY 7개 요일 설정을 모두 보내야 하며, 누락/중복 요일이 있으면 실패합니다."
    )
    @PutMapping("/order-schedule")
    public ApiResponse<OwnerStoreOrderScheduleResponse> saveMyStoreOrderSchedule(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody OwnerStoreOrderScheduleRequest request
    ) {
        OwnerStoreOrderScheduleResponse result = ownerStoreOrderScheduleService.saveMyOrderSchedule(member, request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "내 가게 메뉴 목록 조회", description = "로그인한 사업자 회원의 가게 메뉴 목록을 조회합니다.")
    @GetMapping("/menus")
    public ApiResponse<MenuListResponse> getMyStoreMenus(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        MenuListResponse result = ownerMenuService.getMyStoreMenus(member);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "내 가게 메뉴 상세 조회", description = "로그인한 사업자 회원의 가게 메뉴 상세 정보를 조회합니다.")
    @GetMapping("/menus/{menuId}")
    public ApiResponse<OwnerMenuResponse> getMyStoreMenu(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long menuId
    ) {
        OwnerMenuResponse result = ownerMenuService.getMyStoreMenu(member, menuId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "내 가게 메뉴 등록", description = "로그인한 사업자 회원의 가게에 메뉴를 등록합니다.")
    @PostMapping("/menus")
    public ApiResponse<OwnerMenuResponse> createMenu(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody OwnerMenuRequest request
    ) {
        OwnerMenuResponse result = ownerMenuService.createMenu(member, request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "내 가게 메뉴 수정", description = "로그인한 사업자 회원의 가게 메뉴를 수정합니다.")
    @PutMapping("/menus/{menuId}")
    public ApiResponse<OwnerMenuResponse> updateMenu(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long menuId,
            @Valid @RequestBody OwnerMenuRequest request
    ) {
        OwnerMenuResponse result = ownerMenuService.updateMenu(member, menuId, request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "내 가게 메뉴 삭제", description = "로그인한 사업자 회원의 가게 메뉴를 삭제합니다.")
    @DeleteMapping("/menus/{menuId}")
    public ApiResponse<String> deleteMenu(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable Long menuId
    ) {
        ownerMenuService.deleteMenu(member, menuId);
        return ApiResponse.onSuccess("메뉴가 삭제되었습니다.");
    }
}
