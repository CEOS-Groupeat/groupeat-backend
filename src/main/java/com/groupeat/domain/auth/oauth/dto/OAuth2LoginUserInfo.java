package com.groupeat.domain.auth.oauth.dto;

import com.groupeat.domain.member.enums.OAuthProvider;

public record OAuth2LoginUserInfo(
        OAuthProvider provider,
        String providerUserId,
        String nickname,
        String email
) {
}
