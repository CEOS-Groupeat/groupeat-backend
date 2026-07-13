package com.groupeat.domain.owner.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.owner.dto.request.OwnerProfileUpdateRequest;
import com.groupeat.domain.owner.dto.response.OwnerBusinessProfileResponse;
import com.groupeat.domain.owner.dto.response.OwnerProfileResponse;
import com.groupeat.domain.owner.service.OwnerMyPageService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/mypage")
@RequiredArgsConstructor
@Tag(name = "Owner MyPage", description = "사업자 마이페이지 API")
public class OwnerMyPageController {

    private final OwnerMyPageService ownerMyPageService;

    @GetMapping("/profile")
    @Operation(
            summary = "사업자 프로필 조회",
            description = "로그인한 사업자 회원의 프로필과 연결된 소셜 계정을 조회합니다."
    )
    public ApiResponse<OwnerProfileResponse> getProfile(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(ownerMyPageService.getProfile(member.memberId()));
    }

    @PatchMapping("/profile")
    @Operation(
            summary = "사업자 프로필 수정",
            description = "이메일, 생년월일, 성별을 수정합니다. 실명과 휴대폰 번호는 수정할 수 없습니다."
    )
    public ApiResponse<OwnerProfileResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody OwnerProfileUpdateRequest request
    ) {
        return ApiResponse.onSuccess(ownerMyPageService.updateProfile(member.memberId(), request));
    }

    @GetMapping("/business-profile")
    @Operation(
            summary = "사업자 정보 조회",
            description = "대표자명, 상호명, 개업연월일, 사업자 유형, 사업자등록번호, 사업자등록증 파일 URL을 조회합니다."
    )
    public ApiResponse<OwnerBusinessProfileResponse> getBusinessProfile(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(ownerMyPageService.getBusinessProfile(member.memberId()));
    }
}
