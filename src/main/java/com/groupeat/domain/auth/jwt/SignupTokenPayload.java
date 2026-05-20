package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.enums.OAuthProvider;

public record SignupTokenPayload(
        OAuthProvider provider,
        String providerUserId,
        String nickname,
        String email,
        MemberType memberType
) {
}
