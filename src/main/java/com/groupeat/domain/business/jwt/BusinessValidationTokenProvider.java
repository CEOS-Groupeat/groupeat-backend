package com.groupeat.domain.business.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class BusinessValidationTokenProvider {

    // 토큰의 유효기간 설정: 30분
    private static final long VALIDATION_TOKEN_VALID_TIME = 1000L * 60 * 30;

    private final SecretKey secretKey;

    public BusinessValidationTokenProvider(
            @Value("${jwt.business-secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    // JWT 포장
    public String createValidationToken(String businessRegistrationNumber) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + VALIDATION_TOKEN_VALID_TIME);

        return Jwts.builder()
                .subject("business_validation")
                .claim("businessRegistrationNumber", businessRegistrationNumber)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // 토큰 해석
    public BusinessValidationTokenPayload getPayload(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new BusinessValidationTokenPayload(
                claims.get("businessRegistrationNumber", String.class)
        );
    }
}