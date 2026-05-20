package com.groupeat.domain.auth.oauth.handler;

import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.auth.oauth.userinfo.KakaoOAuth2UserInfo;
import com.groupeat.domain.auth.service.AuthService;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String OAUTH2_MEMBER_TYPE_COOKIE = "OAUTH2_MEMBER_TYPE";

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User = oauthToken.getPrincipal();

        OAuth2LoginUserInfo userInfo = extractOAuth2UserInfo(registrationId, oauth2User);

        Member member = authService.findMemberBySocialAccount(userInfo).orElse(null);

        if (member != null && member.getMemberStatus() == MemberStatus.ACTIVE) {
            // TODO: 기존 회원이면 AccessToken / RefreshToken 발급 후 프론트 메인 페이지로 redirect
            deleteMemberTypeCookie(response);
            response.sendRedirect("/api/auth/oauth2/success-test?status=login");
            return;
        }

        if (member != null && member.getMemberStatus() == MemberStatus.SIGNUP_IN_PROGRESS) {
            deleteMemberTypeCookie(response);
            response.sendRedirect(
                    "/api/auth/oauth2/success-test?status=signup-in-progress"
                            + "&memberId=" + member.getId()
                            + "&memberType=" + member.getMemberType()
                            + "&nextStep=" + getNextStep(member.getMemberType())
            );
            return;
        }

        MemberType memberType = extractMemberTypeFromCookie(request);

        String signupToken = authService.createSignupToken(userInfo, memberType);

        deleteMemberTypeCookie(response);

        String encodedSignupToken = URLEncoder.encode(signupToken, StandardCharsets.UTF_8);

        // TODO: 프론트 회원가입 페이지로 변경
        // 예: http://localhost:3000/signup?signupToken=...
        response.sendRedirect("/api/auth/oauth2/success-test?status=signup&signupToken=" + encodedSignupToken);
    }

    private String getNextStep(MemberType memberType) {
        return switch (memberType) {
            case CUSTOMER -> "CUSTOMER_PROFILE";
            case BUSINESS -> "BUSINESS_PROFILE";
        };
    }

    private OAuth2LoginUserInfo extractOAuth2UserInfo(
            String registrationId,
            OAuth2User oauth2User
    ) {
        return switch (registrationId) {
            case "kakao" -> KakaoOAuth2UserInfo.from(oauth2User);
            // case "google" -> GoogleOAuth2UserInfo.from(oauth2User);
            // case "naver" -> NaverOAuth2UserInfo.from(oauth2User);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        };
    }

    private MemberType extractMemberTypeFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new IllegalArgumentException("회원 유형 정보가 없습니다.");
        }

        for (Cookie cookie : cookies) {
            if (OAUTH2_MEMBER_TYPE_COOKIE.equals(cookie.getName())) {
                return MemberType.valueOf(cookie.getValue());
            }
        }

        throw new IllegalArgumentException("회원 유형 정보가 없습니다.");
    }

    private void deleteMemberTypeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_MEMBER_TYPE_COOKIE, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }
}
