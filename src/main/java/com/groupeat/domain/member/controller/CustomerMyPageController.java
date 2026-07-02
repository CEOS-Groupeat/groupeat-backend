package com.groupeat.domain.member.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.auth.service.AuthCookieService;
import com.groupeat.domain.member.dto.request.CustomerAccountUpdateRequest;
import com.groupeat.domain.member.dto.request.PhoneNumberUpdateRequest;
import com.groupeat.domain.member.dto.response.CustomerAccountResponse;
import com.groupeat.domain.member.dto.response.CustomerMyPageResponse;
import com.groupeat.domain.member.dto.response.MemberWithdrawalResponse;
import com.groupeat.domain.member.dto.response.PhoneNumberUpdateResponse;
import com.groupeat.domain.member.service.CustomerMyPageService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/mypage")
@RequiredArgsConstructor
@Tag(name = "Customer MyPage", description = "고객 마이페이지 API")
public class CustomerMyPageController {

    private final CustomerMyPageService customerMyPageService;
    private final AuthCookieService authCookieService;

    @GetMapping
    @Operation(summary = "마이페이지 요약 조회")
    public ApiResponse<CustomerMyPageResponse> getMyPage(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(customerMyPageService.getMyPage(member.memberId()));
    }

    @GetMapping("/account")
    @Operation(summary = "계정 정보 조회")
    public ApiResponse<CustomerAccountResponse> getAccount(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(customerMyPageService.getAccount(member.memberId()));
    }

    @PatchMapping("/account")
    @Operation(summary = "계정 정보 수정", description = "실명, 이메일, 생년월일, 성별을 수정합니다.")
    public ApiResponse<CustomerAccountResponse> updateAccount(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody CustomerAccountUpdateRequest request
    ) {
        return ApiResponse.onSuccess(customerMyPageService.updateAccount(member.memberId(), request));
    }

    @PatchMapping("/account/phone-number")
    @Operation(summary = "휴대폰 번호 변경", description = "인증이 완료된 새 휴대폰 번호로 변경합니다.")
    public ApiResponse<PhoneNumberUpdateResponse> updatePhoneNumber(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PhoneNumberUpdateRequest request
    ) {
        return ApiResponse.onSuccess(customerMyPageService.updatePhoneNumber(member.memberId(), request));
    }

    @DeleteMapping("/account")
    @Operation(summary = "회원 탈퇴")
    public ApiResponse<MemberWithdrawalResponse> withdraw(
            @AuthenticationPrincipal AuthenticatedMember member,
            HttpServletResponse response
    ) {
        MemberWithdrawalResponse result = customerMyPageService.withdraw(member.memberId());
        authCookieService.clearAuthTokenCookies(response);
        return ApiResponse.onSuccess(result);
    }
}
