package com.groupeat.domain.notification.service.listener;

import com.groupeat.domain.notification.dto.NotificationFcmMessage;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.event.NewOrderRequestNotificationEvent;
import com.groupeat.domain.notification.service.command.NotificationCommandService;
import com.groupeat.domain.notification.service.rabbit.NotificationMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewOrderRequestNotificationListener {

    private final NotificationCommandService notificationCommandService;
    private final NotificationMessagePublisher notificationMessagePublisher;

    // 결제 완료 트랜잭션 커밋 이후 사업자 신규 주문 요청 알림 내역을 저장하고 FCM 발송 작업을 큐에 등록
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createNewOrderRequestNotification(NewOrderRequestNotificationEvent event) {
        try {
            Notification notification = notificationCommandService.createNewOrderRequestNotification(event.orderId());
            notificationMessagePublisher.publishFcmMessage(NotificationFcmMessage.from(notification, data(notification)));
        } catch (RuntimeException e) {
            log.warn("New order request notification creation failed. orderId={}", event.orderId(), e);
        }
    }

    private Map<String, String> data(Notification notification) {
        return Map.of(
                "type", notification.getNotificationType().name(),
                "orderId", String.valueOf(notification.getReferenceId())
        );
    }
}
