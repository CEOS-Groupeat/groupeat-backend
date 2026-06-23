package com.groupeat.domain.admin.business.controller;

import com.groupeat.domain.admin.business.dto.request.AdminVerificationProcessRequest;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationProcessResponse;
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