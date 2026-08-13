package com.groupeat.domain.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app.order")
public record OrderProperties(
        Set<Long> blockedStoreIds
) {
    public OrderProperties {
        blockedStoreIds = blockedStoreIds != null ? Set.copyOf(blockedStoreIds) : Set.of();
    }
}
