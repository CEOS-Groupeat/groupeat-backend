package com.groupeat.domain.notification.service.scheduler;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.notification.config.NotificationSchedulerProperties;
import com.groupeat.domain.notification.dto.FcmSendRequest;
import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.enums.NotificationReferenceType;
import com.groupeat.domain.notification.enums.NotificationType;
import com.groupeat.domain.notification.repository.NotificationRepository;
import com.groupeat.domain.notification.service.command.NotificationCommandService;
import com.groupeat.domain.notification.service.fcm.FcmMessageSender;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.global.config.AppTimeZoneProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickupReminderNotificationScheduler {

    private final NotificationSchedulerProperties schedulerProperties;
    private final AppTimeZoneProperties appTimeZoneProperties;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationCommandService notificationCommandService;
    private final MemberRepository memberRepository;
    private final FcmMessageSender fcmMessageSender;

    // 내일 픽업 예정인 확정 주문에 대해 고객 픽업 하루 전 알림을 생성하고 발송
    @Scheduled(cron = "${app.notification.scheduler.pickup-reminder-cron}", zone = "${app.time-zone}")
    public void sendPickupReminderDayBefore() {
        if (!schedulerProperties.enabled()) {
            return;
        }

        LocalDate pickupDate = LocalDate.now(appTimeZoneProperties.zoneId()).plusDays(1);
        List<Order> orders = orderRepository.findAllByOrderStatusAndPickupDateWithItems(
                OrderStatus.ACCEPTED,
                pickupDate
        );

        for (Order order : orders) {
            if (alreadyCreated(order)) {
                continue;
            }

            try {
                Notification notification = notificationCommandService.createPickupReminderDayBeforeNotification(order.getId());
                sendFcmIfAgreed(order.getMemberId(), notification);
            } catch (RuntimeException e) {
                log.warn("Pickup reminder notification failed. orderId={}", order.getId(), e);
            }
        }
    }

    private boolean alreadyCreated(Order order) {
        return notificationRepository.existsByReceiverMemberIdAndNotificationTypeAndReferenceTypeAndReferenceIdAndDeletedAtIsNull(
                order.getMemberId(),
                NotificationType.PICKUP_REMINDER_DAY_BEFORE,
                NotificationReferenceType.ORDER,
                order.getId()
        );
    }

    private void sendFcmIfAgreed(Long memberId, Notification notification) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null || !member.isOrderStatusNotificationAgreed()) {
            return;
        }

        fcmMessageSender.sendToMember(new FcmSendRequest(
                memberId,
                notification.getTitle(),
                notification.getBody(),
                data(notification)
        ));
    }

    private Map<String, String> data(Notification notification) {
        return Map.of(
                "type", notification.getNotificationType().name(),
                "orderId", String.valueOf(notification.getReferenceId())
        );
    }
}
