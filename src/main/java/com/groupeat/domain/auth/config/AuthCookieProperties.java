package com.groupeat.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        boolean secure,
        String sameSite,
        String domain
) {

    public boolean hasDomain() {
        return domain != null && !domain.isBlank();
    }
}
