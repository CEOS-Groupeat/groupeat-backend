package com.groupeat.domain.auth.oauth.userinfo;

import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.member.enums.OAuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public class KakaoOAuth2UserInfo {

    public static OAuth2LoginUserInfo from(OAuth2User oauth2User) {
        Object id = oauth2User.getAttribute("id");

        String nickname = null;

        Object propertiesObj = oauth2User.getAttribute("properties");
        if (propertiesObj instanceof Map<?, ?> properties) {
            Object nicknameObj = properties.get("nickname");
            if (nicknameObj != null) {
                nickname = nicknameObj.toString();
            }
        }

        return new OAuth2LoginUserInfo(
                OAuthProvider.KAKAO,
                id.toString(),
                nickname,
                null
        );
    }
}
