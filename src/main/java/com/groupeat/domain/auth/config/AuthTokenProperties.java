package com.groupeat.domain.auth.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.auth.token")
public record AuthTokenProperties(
        @Min(1) long accessTokenExpirationMinutes,
        @Min(1) long refreshTokenExpirationDays,
        @Min(1) long signupTokenExpirationMinutes
) {

    public Duration accessTokenExpiration() {
        return Duration.ofMinutes(accessTokenExpirationMinutes);
    }

    public Duration refreshTokenExpiration() {
        return Duration.ofDays(refreshTokenExpirationDays);
    }

    public Duration signupTokenExpiration() {
        return Duration.ofMinutes(signupTokenExpirationMinutes);
    }
}
