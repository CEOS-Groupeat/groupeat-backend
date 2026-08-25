package com.groupeat.domain.notification.service.rabbit;

import com.groupeat.domain.notification.config.NotificationRabbitProperties;
import com.groupeat.domain.notification.dto.NotificationFcmMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationRabbitProperties properties;

    public void publishFcmMessage(NotificationFcmMessage message) {
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), message);
            log.info(
                    "FCM notification message published. messageId={}, notificationId={}, memberId={}, type={}",
                    message.messageId(),
                    message.notificationId(),
                    message.memberId(),
                    message.notificationType()
            );
        } catch (AmqpException e) {
            log.warn(
                    "FCM notification message publish failed. messageId={}, notificationId={}, memberId={}, type={}",
                    message.messageId(),
                    message.notificationId(),
                    message.memberId(),
                    message.notificationType(),
                    e
            );
        }
    }
}
