package com.groupeat.domain.notification.service.listener;

import com.groupeat.domain.notification.event.NewOrderRequestNotificationEvent;
import com.groupeat.domain.notification.service.command.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewOrderRequestNotificationListener {

    private final NotificationCommandService notificationCommandService;

    // 결제 완료 트랜잭션 커밋 이후 사업자 신규 주문 요청 알림 내역 저장
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createNewOrderRequestNotification(NewOrderRequestNotificationEvent event) {
        try {
            notificationCommandService.createNewOrderRequestNotification(event.orderId());
        } catch (RuntimeException e) {
            log.warn("New order request notification creation failed. orderId={}", event.orderId(), e);
        }
    }
}
