package com.groupeat.domain.notification.service.listener;

import com.groupeat.domain.notification.dto.FcmSendRequest;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.event.NewOrderRequestNotificationEvent;
import com.groupeat.domain.notification.service.command.NotificationCommandService;
import com.groupeat.domain.notification.service.fcm.FcmMessageSender;
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
    private final FcmMessageSender fcmMessageSender;

    // 결제 완료 트랜잭션 커밋 이후 사업자 신규 주문 요청 알림 내역 저장 및 FCM 알림 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createNewOrderRequestNotification(NewOrderRequestNotificationEvent event) {
        try {
            Notification notification = notificationCommandService.createNewOrderRequestNotification(event.orderId());
            fcmMessageSender.sendToMember(new FcmSendRequest(
                    notification.getReceiverMemberId(),
                    notification.getTitle(),
                    notification.getBody(),
                    data(notification)
            ));
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
