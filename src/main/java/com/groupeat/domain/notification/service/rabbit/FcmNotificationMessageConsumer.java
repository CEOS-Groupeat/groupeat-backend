package com.groupeat.domain.notification.service.rabbit;

import com.groupeat.domain.notification.config.NotificationRabbitProperties;
import com.groupeat.domain.notification.dto.FcmSendResult;
import com.groupeat.domain.notification.dto.NotificationFcmMessage;
import com.groupeat.domain.notification.service.fcm.FcmMessageSender;
import com.groupeat.domain.notification.service.fcm.NotificationFcmIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationMessageConsumer {

    private static final String X_DEATH = "x-death";
    private static final String QUEUE = "queue";
    private static final String REASON = "reason";
    private static final String COUNT = "count";
    private static final String REJECTED = "rejected";

    private final FcmMessageSender fcmMessageSender;
    private final NotificationFcmIdempotencyService notificationFcmIdempotencyService;
    private final NotificationMessagePublisher notificationMessagePublisher;
    private final NotificationRabbitProperties properties;

    // RabbitMQ에서 알림 메시지를 수신해 FCM 발송 수행
    @RabbitListener(queues = "${app.notification.rabbitmq.queue}")
    public void consume(NotificationFcmMessage fcmMessage, Message rawMessage) {
        try {
            if (notificationFcmIdempotencyService.alreadySent(fcmMessage.notificationId())) {
                log.info(
                        "FCM notification message skipped because already sent. messageId={}, notificationId={}, memberId={}",
                        fcmMessage.messageId(),
                        fcmMessage.notificationId(),
                        fcmMessage.memberId()
                );
                return;
            }

            FcmSendResult result = fcmMessageSender.sendToMember(fcmMessage.toFcmSendRequest());
            if (result.failureCount() > 0) {
                throw new IllegalStateException("FCM send failed. failureCount=" + result.failureCount());
            }
            notificationFcmIdempotencyService.recordSent(fcmMessage);

            log.info(
                    "FCM notification message consumed. messageId={}, notificationId={}, memberId={}, targetCount={}, successCount={}",
                    fcmMessage.messageId(),
                    fcmMessage.notificationId(),
                    fcmMessage.memberId(),
                    result.targetCount(),
                    result.successCount()
            );
        } catch (RuntimeException e) {
            handleFailure(fcmMessage, rawMessage, e);
        }
    }

    // FCM 발송 실패 메시지를 재시도하거나 최대 시도 횟수 초과 시 DLQ로 이동
    private void handleFailure(NotificationFcmMessage fcmMessage, Message rawMessage, RuntimeException e) {
        long currentAttempt = rejectedCount(rawMessage) + 1;
        if (currentAttempt >= properties.maxRetryAttempts()) {
            boolean published = notificationMessagePublisher.publishFcmDeadLetterMessage(fcmMessage);
            if (published) {
                log.warn(
                        "FCM notification message retry exhausted. messageId={}, notificationId={}, memberId={}, attempt={}",
                        fcmMessage.messageId(),
                        fcmMessage.notificationId(),
                        fcmMessage.memberId(),
                        currentAttempt,
                        e
                );
                return;
            }
        }

        log.warn(
                "FCM notification message failed. messageId={}, notificationId={}, memberId={}, attempt={}, maxAttempts={}",
                fcmMessage.messageId(),
                fcmMessage.notificationId(),
                fcmMessage.memberId(),
                currentAttempt,
                properties.maxRetryAttempts(),
                e
        );
        throw new AmqpRejectAndDontRequeueException("FCM notification message failed", e);
    }

    private long rejectedCount(Message message) {
        Object xDeathHeader = message.getMessageProperties().getHeaders().get(X_DEATH);
        if (!(xDeathHeader instanceof List<?> xDeathList)) {
            return 0;
        }

        return xDeathList.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .filter(this::isMainQueueRejectedDeath)
                .mapToLong(this::deathCount)
                .sum();
    }

    private boolean isMainQueueRejectedDeath(Map<?, ?> death) {
        return Objects.equals(String.valueOf(death.get(QUEUE)), properties.queue())
                && Objects.equals(String.valueOf(death.get(REASON)), REJECTED);
    }

    private long deathCount(Map<?, ?> death) {
        Object count = death.get(COUNT);
        return count instanceof Number number ? number.longValue() : 0;
    }
}
