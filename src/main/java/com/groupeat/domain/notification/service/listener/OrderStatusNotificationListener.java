package com.groupeat.domain.notification.service.listener;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.notification.dto.NotificationFcmMessage;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.event.OrderStatusNotificationEvent;
import com.groupeat.domain.notification.service.command.NotificationCommandService;
import com.groupeat.domain.notification.service.rabbit.NotificationMessagePublisher;
import com.groupeat.domain.orders.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusNotificationListener {

    private final MemberRepository memberRepository;
    private final NotificationCommandService notificationCommandService;
    private final NotificationMessagePublisher notificationMessagePublisher;

    // 주문 상태 변경 트랜잭션 커밋 이후 알림 내역을 저장하고 FCM 발송 작업을 큐에 등록
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderStatusNotification(OrderStatusNotificationEvent event) {
        try {
            if (!isNotificationTargetStatus(event.orderStatus())) {
                return;
            }

            Notification notification = notificationCommandService.createCustomerOrderStatusNotification(
                    event.orderId(),
                    event.orderStatus()
            );

            Member member = memberRepository.findById(event.memberId()).orElse(null);
            if (member == null || !member.isOrderStatusNotificationAgreed()) {
                return;
            }

            notificationMessagePublisher.publishFcmMessage(NotificationFcmMessage.from(notification, data(event)));
        } catch (RuntimeException e) {
            log.warn(
                    "Order status FCM notification failed. orderId={}, memberId={}, orderStatus={}",
                    event.orderId(),
                    event.memberId(),
                    event.orderStatus(),
                    e
            );
        }
    }

    private boolean isNotificationTargetStatus(OrderStatus orderStatus) {
        return orderStatus == OrderStatus.ACCEPTED || orderStatus == OrderStatus.REJECTED;
    }

    private Map<String, String> data(OrderStatusNotificationEvent event) {
        return Map.of(
                "type", "ORDER_STATUS_CHANGED",
                "orderId", String.valueOf(event.orderId()),
                "orderStatus", event.orderStatus().name()
        );
    }
}
