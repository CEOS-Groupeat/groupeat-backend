package com.groupeat.domain.auth.controller;

import com.groupeat.domain.auth.dto.AuthenticatedMemberResponse;
import com.groupeat.domain.auth.dto.LogoutResponse;
import com.groupeat.domain.auth.dto.TokenReissueResponse;
import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.auth.service.AuthCookieService;
import com.groupeat.domain.auth.service.AuthService;
import com.groupeat.global.apiPayload.code.status.GlobalErrorStatus;
import com.groupeat.global.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 및 토큰 관리 API")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @GetMapping("/me")
    @Operation(summary = "내 인증 정보 조회", description = "Access token 쿠키 또는 Bearer 토큰으로 현재 로그인한 회원 정보를 조회합니다.")
    public AuthenticatedMemberResponse me(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        if (member == null) {
            throw new GeneralException(GlobalErrorStatus._UNAUTHORIZED);
        }

        return AuthenticatedMemberResponse.from(member);
    }

    @PostMapping("/reissue")
    @Operation(summary = "Access token 재발급", description = "Refresh token 쿠키로 Access token 쿠키를 재발급합니다.")
    public TokenReissueResponse reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = authCookieService.extractRefreshToken(request);
        authService.reissueAccessToken(refreshToken, response);

        return new TokenReissueResponse("Access token이 재발급되었습니다.");
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "인증 쿠키를 삭제합니다.")
    public LogoutResponse logout(HttpServletResponse response) {
        authCookieService.clearAuthTokenCookies(response);

        return new LogoutResponse("로그아웃되었습니다.");
    }
}
