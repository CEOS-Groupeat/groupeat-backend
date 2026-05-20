package com.groupeat.domain.auth.controller;

import com.groupeat.domain.member.enums.MemberType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
        Cookie cookie = new Cookie(OAUTH2_MEMBER_TYPE_COOKIE, memberType.name());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(180);

        response.addCookie(cookie);

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/" + provider)
                .build();
    }
}
