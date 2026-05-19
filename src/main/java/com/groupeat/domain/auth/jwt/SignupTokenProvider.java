package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.member.enums.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class SignupTokenProvider {

    private static final long SIGNUP_TOKEN_VALID_TIME = 1000L * 60 * 10; // 10분

    private final SecretKey secretKey;

    public SignupTokenProvider(
            @Value( "${jwt.signup-secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createSignupToken(OAuth2LoginUserInfo userInfo, MemberType memberType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + SIGNUP_TOKEN_VALID_TIME);

        return Jwts.builder()
                .subject("signup")
                .claim("provider", userInfo.provider().name())
                .claim("providerUserId", userInfo.providerUserId())
                .claim("nickname", userInfo.nickname())
                .claim("email", userInfo.email())
                .claim("memberType", memberType.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseSignupToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
