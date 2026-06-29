package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.auth.exception.AuthErrorStatus;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.global.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
        Claims claims = parseToken(token, accessSecretKey);

        validateTokenType(claims, "access");

        Number memberId = claims.get("memberId", Number.class);

        Boolean isAdminClaim = claims.get("isAdmin", Boolean.class);
        boolean isAdmin = (isAdminClaim != null) ? isAdminClaim : false;

        return new AuthenticatedMember(
                memberId.longValue(),
                MemberType.valueOf(claims.get("memberType", String.class)),
                MemberStatus.valueOf(claims.get("memberStatus", String.class)),
                isAdmin
        );
    }

    public Long parseRefreshTokenMemberId(String token) {
        Claims claims = parseToken(token, refreshSecretKey);

        validateTokenType(claims, "refresh");

        Number memberId = claims.get("memberId", Number.class);
        return memberId.longValue();
    }

    private Claims parseToken(String token, SecretKey secretKey) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new GeneralException(AuthErrorStatus.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }
    }

    private void validateTokenType(Claims claims, String expectedTokenType) {
        String tokenType = claims.get("tokenType", String.class);

        if ("access".equals(expectedTokenType) && !expectedTokenType.equals(tokenType)) {
            throw new GeneralException(AuthErrorStatus.NOT_ACCESS_TOKEN);
        }

        if ("refresh".equals(expectedTokenType) && !expectedTokenType.equals(tokenType)) {
            throw new GeneralException(AuthErrorStatus.NOT_REFRESH_TOKEN);
        }
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
                .claim("isAdmin", member.isAdmin())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}
