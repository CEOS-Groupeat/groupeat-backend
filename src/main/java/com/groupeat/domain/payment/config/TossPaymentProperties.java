package com.groupeat.domain.payment.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentProperties(
        // 외부에 노출되면 안 되는 키 (환경변수로 주입)
        @NotBlank String secretKey,
        @NotBlank String confirmUrl,
        @NotBlank String cancelBaseUrl
) {
}
