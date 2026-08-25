package com.groupeat.domain.notification.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.notification.rabbitmq")
public record NotificationRabbitProperties(
        // FCM 알림 메시지를 최초 발행하는 메인 exchange
        @NotBlank
        String exchange,

        // 메인 queue로 라우팅할 때 사용하는 routing key
        @NotBlank
        String routingKey,

        // Consumer가 FCM 발송 작업을 처리하는 메인 queue
        @NotBlank
        String queue,

        // 메인 queue 처리 실패 메시지를 재시도 queue로 보내는 exchange
        @NotBlank
        String retryExchange,

        // 재시도 queue로 라우팅할 때 사용하는 routing key
        @NotBlank
        String retryRoutingKey,

        // TTL 만료 후 메시지를 메인 exchange로 되돌리는 재시도 queue
        @NotBlank
        String retryQueue,

        // 재시도 queue에서 대기할 시간
        @Positive
        long retryTtlMs,

        // DLQ 이동 전 허용할 최대 처리 시도 횟수
        @Positive
        int maxRetryAttempts,

        // 최대 재시도 초과 메시지를 최종 DLQ로 보내는 exchange
        @NotBlank
        String deadLetterExchange,

        // 최종 DLQ로 라우팅할 때 사용하는 routing key
        @NotBlank
        String deadLetterRoutingKey,

        // 반복 실패 메시지를 보관하는 최종 DLQ
        @NotBlank
        String deadLetterQueue
) {
}
