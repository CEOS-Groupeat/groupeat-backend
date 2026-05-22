package com.groupeat.domain.auth.controller;

import com.groupeat.domain.member.enums.MemberType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/oauth2")
@Tag(name = "OAuth2", description = "소셜 로그인 시작 API")
public class OAuth2AuthorizationController {

    private static final String OAUTH2_MEMBER_TYPE_COOKIE = "OAUTH2_MEMBER_TYPE";

    @GetMapping("/authorize/{provider}")
    @Operation(summary = "소셜 로그인 시작", description = "회원 유형을 저장한 뒤 선택한 소셜 로그인 페이지로 이동합니다.")
    public ResponseEntity<Void> authorize(
            @PathVariable String provider,
            @RequestParam MemberType memberType,
            HttpServletResponse response
    ) {
        ResponseCookie cookie = ResponseCookie.from(OAUTH2_MEMBER_TYPE_COOKIE, memberType.name())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(180)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/" + provider)
                .build();
    }
}
