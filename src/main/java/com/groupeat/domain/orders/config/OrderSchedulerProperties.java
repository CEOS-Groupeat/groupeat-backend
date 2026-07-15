package com.groupeat.domain.orders.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.order.scheduler")
public record OrderSchedulerProperties(
        boolean enabled,

        @Min(60000)
        long autoRejectFixedDelayMs
) {
}
