package com.groupeat.domain.owner.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.owner.service.OwnerMyPageTermsService;
import com.groupeat.domain.terms.dto.CustomerTermsDetailResponse;
import com.groupeat.domain.terms.dto.CustomerTermsResponse;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/mypage/terms")
@RequiredArgsConstructor
@Tag(name = "Owner MyPage Terms", description = "사업자 마이페이지 약관 API")
public class OwnerMyPageTermsController {

    private final OwnerMyPageTermsService ownerMyPageTermsService;

    @GetMapping
    @Operation(
            summary = "사업자 필수 약관 목록 조회",
            description = "약관 페이지에서 노출할 COMMON 및 BUSINESS 대상의 활성 필수 약관을 현재 회원의 동의 상태와 함께 조회합니다."
    )
    public ApiResponse<List<CustomerTermsResponse>> getTerms(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(ownerMyPageTermsService.getTerms(member.memberId()));
    }

    @GetMapping("/{termsId}")
    @Operation(
            summary = "사업자 필수 약관 전문 조회",
            description = "약관 페이지에서 노출할 COMMON 또는 BUSINESS 대상 활성 필수 약관의 전문을 조회합니다."
    )
    public ApiResponse<CustomerTermsDetailResponse> getTermsDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Parameter(description = "조회할 약관 ID", example = "1")
            @PathVariable Long termsId
    ) {
        return ApiResponse.onSuccess(ownerMyPageTermsService.getTermsDetail(member.memberId(), termsId));
    }
}
