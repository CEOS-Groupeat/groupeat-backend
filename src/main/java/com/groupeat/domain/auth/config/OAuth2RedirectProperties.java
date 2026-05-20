package com.groupeat.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend.oauth-redirect")
public record OAuth2RedirectProperties(
        String baseUrl,
        String loginSuccessPath,
        String signupPath,
        String signupInProgressPath
) {

    public String loginSuccessUrl() {
        return buildUrl(loginSuccessPath);
    }

    public String signupUrl() {
        return buildUrl(signupPath);
    }

    public String signupInProgressUrl() {
        return buildUrl(signupInProgressPath);
    }

    private String buildUrl(String path) {
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        return normalizedBaseUrl + normalizedPath;
    }
}
