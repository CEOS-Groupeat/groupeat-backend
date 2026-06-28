package com.groupeat.domain.admin.business.controller;

import com.groupeat.domain.admin.business.dto.request.AdminVerificationProcessRequest;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationDetailResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationListResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationProcessResponse;
import com.groupeat.domain.admin.business.enums.AdminVerificationFilterType;
import com.groupeat.domain.admin.business.service.AdminBusinessVerificationService;
import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Business Verification", description = "관리자 사업자 인증 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/business-verifications")
public class AdminBusinessVerificationController {

    private final AdminBusinessVerificationService adminService;

    @Operation(summary = "사업자 인증 요청 목록 조회", description = "관리자가 상태별(대기/승인/반려)로 사업자 인증 요청 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<AdminVerificationListResponse.VerificationListDTO> getVerificationList(
            @AuthenticationPrincipal AuthenticatedMember admin,
            @RequestParam(required = false) AdminVerificationFilterType filter,
            @RequestParam(required = false) Long lastProfileId,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (admin.memberType() != MemberType.ADMIN) {
            throw new GeneralException(GlobalErrorStatus._FORBIDDEN); // 권한 없음 에러(403)
        }

        return ApiResponse.onSuccess(adminService.getVerificationList(filter, lastProfileId, size));
    }

    @Operation(summary = "사업자 인증 요청 상세 조회", description = "특정 사업자의 회원 정보와 사업자 등록 상세 정보를 조회합니다.")
    @GetMapping("/{profileId}")
    public ApiResponse<AdminVerificationDetailResponse> getVerificationDetail(
            @AuthenticationPrincipal AuthenticatedMember admin,
            @PathVariable Long profileId
    ) {
        return ApiResponse.onSuccess(adminService.getVerificationDetail(profileId));
    }

    @Operation(summary = "사업자 인증 처리 (승인/반려)", description = "관리자가 사업자 가입 요청을 승인하거나 반려합니다.")
    @PatchMapping("/{profileId}/status")
    public ApiResponse<AdminVerificationProcessResponse> processVerification(
            @AuthenticationPrincipal AuthenticatedMember admin,
            @PathVariable Long profileId,
            @Valid @RequestBody AdminVerificationProcessRequest request
    ) {
        Long adminId = admin.memberId();

        AdminVerificationProcessResponse response = adminService.processVerification(adminId, profileId, request);

        return ApiResponse.onSuccess(response);
    }
}