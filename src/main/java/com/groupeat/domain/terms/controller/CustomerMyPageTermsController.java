package com.groupeat.domain.terms.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.terms.dto.CustomerTermsDetailResponse;
import com.groupeat.domain.terms.dto.CustomerTermsResponse;
import com.groupeat.domain.terms.dto.TermsAgreementUpdateRequest;
import com.groupeat.domain.terms.service.CustomerMyPageTermsService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/mypage/terms")
@RequiredArgsConstructor
@Tag(name = "Customer MyPage Terms", description = "고객 마이페이지 약관 API")
public class CustomerMyPageTermsController {

    private final CustomerMyPageTermsService customerMyPageTermsService;

    @GetMapping
    @Operation(
            summary = "고객 약관 목록 조회",
            description = "COMMON 및 CUSTOMER 대상의 모든 활성 약관을 현재 회원의 동의 상태와 함께 조회합니다. modifiable은 선택 약관에서만 true입니다."
    )
    public ApiResponse<List<CustomerTermsResponse>> getTerms(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(customerMyPageTermsService.getTerms(member.memberId()));
    }

    @GetMapping("/{termsId}")
    @Operation(
            summary = "고객 약관 전문 조회",
            description = "동의 여부와 관계없이 COMMON 또는 CUSTOMER 대상 활성 약관의 전문을 조회합니다."
    )
    public ApiResponse<CustomerTermsDetailResponse> getTermsDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Parameter(description = "조회할 약관 ID", example = "1")
            @PathVariable Long termsId
    ) {
        return ApiResponse.onSuccess(customerMyPageTermsService.getTermsDetail(member.memberId(), termsId));
    }

    @PatchMapping("/{termsId}")
    @Operation(
            summary = "선택 약관 동의 상태 변경",
            description = "선택 약관에 동의하거나 기존 동의를 철회합니다. 필수 약관 요청은 TERMS4002 오류를 반환합니다."
    )
    public ApiResponse<CustomerTermsResponse> updateTermsAgreement(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Parameter(description = "변경할 선택 약관 ID", example = "3")
            @PathVariable Long termsId,
            @Valid @RequestBody TermsAgreementUpdateRequest request
    ) {
        return ApiResponse.onSuccess(
                customerMyPageTermsService.updateTermsAgreement(member.memberId(), termsId, request)
        );
    }
}
