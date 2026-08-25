package com.groupeat.domain.notification.service.fcm;

import com.groupeat.domain.notification.config.NotificationRabbitProperties;
import com.groupeat.domain.notification.dto.NotificationFcmMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class NotificationFcmIdempotencyService {

    private static final String SENT_KEY_PREFIX = "notification:fcm:sent:";

    private final StringRedisTemplate stringRedisTemplate;
    private final NotificationRabbitProperties properties;

    // notificationId 기준으로 Redis에 이미 FCM 발송 성공 키가 있는지 확인
    public boolean alreadySent(Long notificationId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(sentKey(notificationId)));
    }

    // FCM 발송 성공 키를 TTL과 함께 저장해 동일 알림 메시지 재전달 시 중복 발송을 방지
    public void recordSent(NotificationFcmMessage message) {
        stringRedisTemplate.opsForValue().set(
                sentKey(message.notificationId()),
                message.messageId(),
                Duration.ofSeconds(properties.sentKeyTtlSeconds())
        );
    }

    private String sentKey(Long notificationId) {
        return SENT_KEY_PREFIX + notificationId;
    }
}
