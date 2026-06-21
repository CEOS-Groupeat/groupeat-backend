package com.groupeat.domain.store.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.service.OwnerStoreService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Owner Store", description = "사업자 가게 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/store")
public class OwnerStoreController {

    private final OwnerStoreService ownerStoreService;

    @Operation(summary = "내 가게 조회", description = "로그인한 사업자 회원의 가게 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<OwnerStoreResponse> getMyStore(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        OwnerStoreResponse result = ownerStoreService.getMyStore(member);
        return ApiResponse.onSuccess(result);
    }
}
