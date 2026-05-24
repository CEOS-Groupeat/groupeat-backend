package com.groupeat.domain.auth.oauth.handler;

import com.groupeat.domain.auth.config.OAuth2RedirectProperties;
import com.groupeat.domain.auth.exception.AuthErrorStatus;
import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.auth.oauth.userinfo.GoogleOAuth2UserInfo;
import com.groupeat.domain.auth.oauth.userinfo.KakaoOAuth2UserInfo;
import com.groupeat.domain.auth.oauth.userinfo.NaverOAuth2UserInfo;
import com.groupeat.domain.auth.service.AuthCookieService;
import com.groupeat.domain.auth.service.AuthService;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.global.exception.GeneralException;
import jakarta.servlet.ServletException;
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

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final OAuth2RedirectProperties oAuth2RedirectProperties;

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
            authCookieService.addAuthTokenCookies(response, member);
            response.sendRedirect(oAuth2RedirectProperties.loginSuccessUrl(member.getMemberType()));
            return;
        }

        if (member != null && member.getMemberStatus() == MemberStatus.SIGNUP_IN_PROGRESS) {
            response.sendRedirect(buildSignupInProgressRedirectUrl(member));
            return;
        }

        String signupToken = authService.createSignupToken(userInfo);

        String encodedSignupToken = URLEncoder.encode(signupToken, StandardCharsets.UTF_8);

        response.sendRedirect(oAuth2RedirectProperties.signupUrl() + "?signupToken=" + encodedSignupToken);
    }

    private String buildSignupInProgressRedirectUrl(Member member) {
        return oAuth2RedirectProperties.signupInProgressUrl()
                + "?memberId=" + member.getId()
                + "&memberType=" + member.getMemberType()
                + "&nextStep=" + getNextStep(member.getMemberType());
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
            case "google" -> GoogleOAuth2UserInfo.from(oauth2User);
            case "naver" -> NaverOAuth2UserInfo.from(oauth2User);
            default -> throw new GeneralException(AuthErrorStatus.UNSUPPORTED_OAUTH_PROVIDER);
        };
    }

}
