package com.groupeat.domain.notification.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.notification.scheduler")
public record NotificationSchedulerProperties(
        boolean enabled,

        @Min(60000)
        long deadlineCheckFixedDelayMs,

        String pickupReminderCron
) {
}
