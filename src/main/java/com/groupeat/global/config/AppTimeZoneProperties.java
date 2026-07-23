package com.groupeat.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@ConfigurationProperties(prefix = "app")
public record AppTimeZoneProperties(
        String timeZone
) {

    public ZoneId zoneId() {
        return ZoneId.of(timeZone);
    }
}
