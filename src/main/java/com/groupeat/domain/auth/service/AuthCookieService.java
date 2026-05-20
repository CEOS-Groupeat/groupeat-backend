package com.groupeat.domain.auth.service;

import com.groupeat.domain.auth.jwt.AuthTokenProvider;
import com.groupeat.domain.member.entity.Member;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    private final AuthTokenProvider authTokenProvider;

    public void addAccessTokenCookie(HttpServletResponse response, Member member) {
        String accessToken = authTokenProvider.createAccessToken(member);
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(
                ACCESS_TOKEN_COOKIE,
                accessToken,
                AuthTokenProvider.ACCESS_TOKEN_VALID_TIME.toSeconds()
        ));
    }

    public void addRefreshTokenCookie(HttpServletResponse response, Member member) {
        String refreshToken = authTokenProvider.createRefreshToken(member);
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(
                REFRESH_TOKEN_COOKIE,
                refreshToken,
                AuthTokenProvider.REFRESH_TOKEN_VALID_TIME.toSeconds()
        ));
    }

    public void addAuthTokenCookies(HttpServletResponse response, Member member) {
        addAccessTokenCookie(response, member);
        addRefreshTokenCookie(response, member);
    }

    public void clearAuthTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(ACCESS_TOKEN_COOKIE, "", 0));
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(REFRESH_TOKEN_COOKIE, "", 0));
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new IllegalArgumentException(cookieName + " 쿠키가 없습니다.");
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new IllegalArgumentException(cookieName + " 쿠키가 없습니다.");
    }

    private String createCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build()
                .toString();
    }
}
