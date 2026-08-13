package com.groupeat.domain.business.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "nts.api")
public record NtsApiProperties(
        @NotBlank
        String baseUrl,

        @NotBlank
        String serviceKey
) { }
