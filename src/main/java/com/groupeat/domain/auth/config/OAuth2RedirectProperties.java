package com.groupeat.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.groupeat.domain.member.enums.MemberType;

@ConfigurationProperties(prefix = "app.frontend.oauth-redirect")
public record OAuth2RedirectProperties(
        String baseUrl,
        String customerLoginSuccessPath,
        String businessLoginSuccessPath,
        String signupPath,
        String signupInProgressPath
) {

    public String loginSuccessUrl(MemberType memberType) {
        return switch (memberType) {
            case CUSTOMER -> buildUrl(customerLoginSuccessPath);
            case BUSINESS -> buildUrl(businessLoginSuccessPath);
        };
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
