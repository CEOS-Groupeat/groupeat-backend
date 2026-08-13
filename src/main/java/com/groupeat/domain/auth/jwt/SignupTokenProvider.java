package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.auth.config.AuthTokenProperties;
import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.member.enums.OAuthProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class SignupTokenProvider {

    private final SecretKey secretKey;
    private final AuthTokenProperties authTokenProperties;

    public SignupTokenProvider(
            @Value( "${jwt.signup-secret}") String secret,
            AuthTokenProperties authTokenProperties
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.authTokenProperties = authTokenProperties;
    }

    public String createSignupToken(OAuth2LoginUserInfo userInfo) {
        Date now = new Date();
        Duration validTime = authTokenProperties.signupTokenExpiration();
        Date expiry = new Date(now.getTime() + validTime.toMillis());

        return Jwts.builder()
                .subject("signup")
                .claim("provider", userInfo.provider().name())
                .claim("providerUserId", userInfo.providerUserId())
                .claim("email", userInfo.email())
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

    public SignupTokenPayload getPayload(String token) {
        Claims claims = parseSignupToken(token);

        return new SignupTokenPayload(
                OAuthProvider.valueOf(claims.get("provider", String.class)),
                claims.get("providerUserId", String.class),
                claims.get("email", String.class)
        );
    }
}
