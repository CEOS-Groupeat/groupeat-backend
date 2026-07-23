package com.groupeat.domain.business.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.business.validation-token")
public record BusinessValidationTokenProperties(
        @Min(1) long expirationMinutes
) {

    public Duration expiration() {
        return Duration.ofMinutes(expirationMinutes);
    }
}
