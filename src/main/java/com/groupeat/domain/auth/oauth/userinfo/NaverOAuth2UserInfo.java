package com.groupeat.domain.auth.oauth.userinfo;

import com.groupeat.domain.auth.exception.AuthErrorStatus;
import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.member.enums.OAuthProvider;
import com.groupeat.global.exception.GeneralException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public class NaverOAuth2UserInfo {

    public static OAuth2LoginUserInfo from(OAuth2User oauth2User) {
        Object responseObj = oauth2User.getAttribute("response");

        if (!(responseObj instanceof Map<?, ?> response)) {
            throw new GeneralException(AuthErrorStatus.INVALID_OAUTH_USER_INFO);
        }

        Object id = response.get("id");

        if (id == null) {
            throw new GeneralException(AuthErrorStatus.INVALID_OAUTH_USER_INFO);
        }

        return new OAuth2LoginUserInfo(
                OAuthProvider.NAVER,
                id.toString(),
                toStringOrNull(response.get("nickname")),
                toStringOrNull(response.get("email"))
        );
    }

    private static String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}
