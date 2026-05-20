package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
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
public class AuthTokenProvider {

    public static final Duration ACCESS_TOKEN_VALID_TIME = Duration.ofMinutes(30);
    public static final Duration REFRESH_TOKEN_VALID_TIME = Duration.ofDays(14);

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;

    public AuthTokenProvider(
            @Value("${jwt.access-secret}") String accessSecret,
            @Value("${jwt.refresh-secret}") String refreshSecret
    ) {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshSecretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Member member) {
        return createToken(member, "access", ACCESS_TOKEN_VALID_TIME, accessSecretKey);
    }

    public String createRefreshToken(Member member) {
        return createToken(member, "refresh", REFRESH_TOKEN_VALID_TIME, refreshSecretKey);
    }

    public AuthenticatedMember parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(accessSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get("tokenType", String.class);
        if (!"access".equals(tokenType)) {
            throw new IllegalArgumentException("Access token이 아닙니다.");
        }

        Number memberId = claims.get("memberId", Number.class);

        return new AuthenticatedMember(
                memberId.longValue(),
                MemberType.valueOf(claims.get("memberType", String.class)),
                MemberStatus.valueOf(claims.get("memberStatus", String.class))
        );
    }

    private String createToken(
            Member member,
            String tokenType,
            Duration validTime,
            SecretKey secretKey
    ) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validTime.toMillis());

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("tokenType", tokenType)
                .claim("memberId", member.getId())
                .claim("memberType", member.getMemberType().name())
                .claim("memberStatus", member.getMemberStatus().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}
