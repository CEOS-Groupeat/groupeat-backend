package com.groupeat.domain.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.business.validation")
public record BusinessValidationProperties(
        boolean ntsBypassEnabled
) {
}
