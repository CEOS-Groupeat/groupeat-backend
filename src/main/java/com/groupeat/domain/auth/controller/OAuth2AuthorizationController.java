package com.groupeat.domain.auth.controller;

import com.groupeat.domain.member.enums.MemberType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2AuthorizationController {

    private static final String OAUTH2_MEMBER_TYPE_COOKIE = "OAUTH2_MEMBER_TYPE";

    @GetMapping("/authorize/{provider}")
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
