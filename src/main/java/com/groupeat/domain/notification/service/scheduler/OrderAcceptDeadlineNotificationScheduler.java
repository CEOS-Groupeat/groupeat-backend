package com.groupeat.domain.notification.service.scheduler;

import com.groupeat.domain.notification.config.NotificationSchedulerProperties;
import com.groupeat.domain.notification.dto.NotificationFcmMessage;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.enums.NotificationReferenceType;
import com.groupeat.domain.notification.enums.NotificationType;
import com.groupeat.domain.notification.repository.NotificationRepository;
import com.groupeat.domain.notification.service.command.NotificationCommandService;
import com.groupeat.domain.notification.service.rabbit.NotificationMessagePublisher;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAcceptDeadlineNotificationScheduler {

    private final NotificationSchedulerProperties schedulerProperties;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationCommandService notificationCommandService;
    private final NotificationMessagePublisher notificationMessagePublisher;

    // 결제 완료 후 미승인 상태인 주문에 대해 사업자 주문 수락 마감 알림을 생성하고 FCM 발송 작업을 큐에 등록
    @Scheduled(fixedDelayString = "${app.notification.scheduler.deadline-check-fixed-delay-ms}")
    public void sendOrderAcceptDeadlineNotifications() {
        if (!schedulerProperties.enabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        sendDeadlineNotifications(
                NotificationType.ORDER_ACCEPT_DEADLINE_12H,
                now.minusHours(schedulerProperties.orderAcceptDeadlineWarningStartHours()),
                now.minusHours(schedulerProperties.orderAcceptDeadlineWarningMiddleHours())
        );
        sendDeadlineNotifications(
                NotificationType.ORDER_ACCEPT_DEADLINE_1H,
                now.minusHours(schedulerProperties.orderAcceptDeadlineWarningEndHours()),
                now.minusHours(schedulerProperties.orderAcceptDeadlineWarningStartHours())
        );
    }

    private void sendDeadlineNotifications(
            NotificationType notificationType,
            LocalDateTime approvedAfter,
            LocalDateTime approvedAtOrBefore
    ) {
        List<Payment> payments = paymentRepository.findAllOrderAcceptDeadlineCandidates(
                PaymentStatus.DONE,
                OrderStatus.PAID,
                approvedAfter,
                approvedAtOrBefore
        );

        for (Payment payment : payments) {
            Order order = payment.getOrder();
            if (alreadyCreated(order, notificationType)) {
                continue;
            }

            try {
                Notification notification = notificationCommandService.createOrderAcceptDeadlineNotification(
                        order.getId(),
                        notificationType
                );
                publishFcmMessage(notification);
            } catch (RuntimeException e) {
                log.warn(
                        "Order accept deadline notification failed. orderId={}, notificationType={}",
                        order.getId(),
                        notificationType,
                        e
                );
            }
        }
    }

    private boolean alreadyCreated(Order order, NotificationType notificationType) {
        return notificationRepository.existsByReceiverMemberIdAndNotificationTypeAndReferenceTypeAndReferenceIdAndDeletedAtIsNull(
                order.getStore().getOwnerId(),
                notificationType,
                NotificationReferenceType.ORDER,
                order.getId()
        );
    }

    private void publishFcmMessage(Notification notification) {
        notificationMessagePublisher.publishFcmMessage(NotificationFcmMessage.from(notification, data(notification)));
    }

    private Map<String, String> data(Notification notification) {
        return Map.of(
                "type", notification.getNotificationType().name(),
                "orderId", String.valueOf(notification.getReferenceId())
        );
    }
}
