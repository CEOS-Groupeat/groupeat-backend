package com.groupeat.domain.auth.controller;

import com.groupeat.domain.auth.dto.AuthenticatedMemberResponse;
import com.groupeat.domain.auth.dto.TokenReissueResponse;
import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.auth.service.AuthCookieService;
import com.groupeat.domain.auth.service.AuthService;
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
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @GetMapping("/me")
    public AuthenticatedMemberResponse me(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        return AuthenticatedMemberResponse.from(member);
    }

    @PostMapping("/reissue")
    public TokenReissueResponse reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = authCookieService.extractRefreshToken(request);
        authService.reissueAccessToken(refreshToken, response);

        return new TokenReissueResponse("Access token이 재발급되었습니다.");
    }
}
