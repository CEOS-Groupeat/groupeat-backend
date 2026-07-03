package com.groupeat.domain.member.controller;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.auth.service.AuthCookieService;
import com.groupeat.domain.member.controller.docs.CustomerWithdrawalApiDocs;
import com.groupeat.domain.member.dto.request.CustomerAccountUpdateRequest;
import com.groupeat.domain.member.dto.request.PhoneNumberUpdateRequest;
import com.groupeat.domain.member.dto.response.CustomerAccountResponse;
import com.groupeat.domain.member.dto.response.CustomerMyPageResponse;
import com.groupeat.domain.member.dto.response.CustomerWithdrawalResponse;
import com.groupeat.domain.member.dto.response.PhoneNumberUpdateResponse;
import com.groupeat.domain.member.service.CustomerMyPageService;
import com.groupeat.domain.member.service.CustomerWithdrawalService;
import com.groupeat.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final CustomerWithdrawalService customerWithdrawalService;
    private final AuthCookieService authCookieService;

    @GetMapping
    @Operation(
            summary = "마이페이지 요약 조회",
            description = "로그인한 고객의 전체 주문 수를 조회합니다. 리뷰와 즐겨찾기 수는 기능 구현 전까지 0으로 반환합니다."
    )
    public ApiResponse<CustomerMyPageResponse> getMyPage(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(customerMyPageService.getMyPage(member.memberId()));
    }

    @GetMapping("/account")
    @Operation(
            summary = "계정 정보 조회",
            description = "로그인한 고객의 기본 정보와 연결된 소셜 계정을 조회합니다. 소셜 제공자가 이메일을 제공하지 않으면 socialAccount.email은 빈 문자열입니다."
    )
    public ApiResponse<CustomerAccountResponse> getAccount(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return ApiResponse.onSuccess(customerMyPageService.getAccount(member.memberId()));
    }

    @PatchMapping("/account")
    @Operation(
            summary = "계정 정보 수정",
            description = "실명, 이메일, 생년월일, 성별을 한 번에 수정합니다. 실명은 필수이며 이메일 빈 문자열은 null로 저장됩니다. 휴대폰 번호는 별도 API를 사용합니다."
    )
    public ApiResponse<CustomerAccountResponse> updateAccount(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody CustomerAccountUpdateRequest request
    ) {
        return ApiResponse.onSuccess(customerMyPageService.updateAccount(member.memberId(), request));
    }

    @PatchMapping("/account/phone-number")
    @Operation(
            summary = "휴대폰 번호 변경",
            description = "POST /api/phone-verifications/send 및 /confirm을 순서대로 호출한 후, 동일한 새 번호를 전달합니다. 인증 정보는 번호 변경 성공 시 사용 처리됩니다."
    )
    public ApiResponse<PhoneNumberUpdateResponse> updatePhoneNumber(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PhoneNumberUpdateRequest request
    ) {
        return ApiResponse.onSuccess(customerMyPageService.updatePhoneNumber(member.memberId(), request));
    }

    @DeleteMapping("/account")
    @CustomerWithdrawalApiDocs
    public ApiResponse<CustomerWithdrawalResponse> withdraw(
            @AuthenticationPrincipal AuthenticatedMember member,
            HttpServletResponse response
    ) {
        CustomerWithdrawalResponse result = customerWithdrawalService.withdraw(member.memberId());
        authCookieService.clearAuthTokenCookies(response);
        return ApiResponse.onSuccess(result);
    }

}
