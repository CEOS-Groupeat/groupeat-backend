package com.groupeat.domain.business.jwt;

import com.groupeat.domain.business.config.BusinessValidationTokenProperties;
import com.groupeat.domain.business.exception.BusinessErrorStatus;
import com.groupeat.global.exception.GeneralException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class BusinessValidationTokenProvider {

    private final SecretKey secretKey;
    private final BusinessValidationTokenProperties tokenProperties;

    public BusinessValidationTokenProvider(
            @Value("${jwt.business-secret}") String secret,
            BusinessValidationTokenProperties tokenProperties
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenProperties = tokenProperties;
    }


    // JWT 포장
    public String createValidationToken(String businessRegistrationNumber) {
        Date now = new Date();
        Duration validTime = tokenProperties.expiration();
        Date expiry = new Date(now.getTime() + validTime.toMillis());

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
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new BusinessValidationTokenPayload(
                    claims.get("businessRegistrationNumber", String.class)
            );

            // 토큰 만료 에러 캐치
        } catch (ExpiredJwtException e) {
            throw new GeneralException(BusinessErrorStatus.BUSINESS_TOKEN_EXPIRED);

            // 토큰 형식 불량, 서명 오류, 비어있음 등 에러 캐치
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new GeneralException(BusinessErrorStatus.INVALID_BUSINESS_TOKEN);
        }
    }
}
