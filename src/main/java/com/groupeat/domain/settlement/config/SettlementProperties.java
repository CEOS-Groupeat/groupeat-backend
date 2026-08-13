package com.groupeat.domain.settlement.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.settlement")
public record SettlementProperties(
        @Min(0) @Max(100) int platformFeeRate
) {
}
