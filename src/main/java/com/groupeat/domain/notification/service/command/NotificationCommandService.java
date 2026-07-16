package com.groupeat.domain.notification.service.command;

import com.groupeat.domain.notification.entity.Notification;
import com.groupeat.domain.notification.enums.NotificationReferenceType;
import com.groupeat.domain.notification.enums.NotificationType;
import com.groupeat.domain.notification.repository.NotificationRepository;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    // 고객에게 보여줄 주문 승인/거절 알림 내역을 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createCustomerOrderStatusNotification(Long orderId, OrderStatus orderStatus) {
        Order order = getOrderWithItems(orderId);
        NotificationType type = toCustomerOrderStatusNotificationType(orderStatus);

        Notification notification = notificationRepository.save(Notification.create(
                order.getMemberId(),
                type,
                customerTitle(type),
                customerBody(type, order.getStore().getStoreName()),
                order.getStore().getStoreName(),
                menuSummary(order.getOrderItems()),
                order.getPickupDate(),
                order.getPickupTime(),
                NotificationReferenceType.ORDER,
                order.getId()
        ));
        log.info(
                "Customer order status notification created. notificationId={}, orderId={}, memberId={}, type={}",
                notification.getId(),
                order.getId(),
                order.getMemberId(),
                type
        );
        return notification;
    }

    // 사업자에게 보여줄 신규 주문 요청 알림 내역을 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createNewOrderRequestNotification(Long orderId) {
        Order order = getOrderWithItems(orderId);

        Notification notification = notificationRepository.save(Notification.create(
                order.getStore().getOwnerId(),
                NotificationType.NEW_ORDER_REQUEST,
                "새로운 주문이 접수되었습니다",
                "새로운 주문 요청을 확인해 주세요.",
                order.getStore().getStoreName(),
                menuSummary(order.getOrderItems()),
                order.getPickupDate(),
                order.getPickupTime(),
                NotificationReferenceType.ORDER,
                order.getId()
        ));
        log.info(
                "New order request notification created. notificationId={}, orderId={}, ownerId={}",
                notification.getId(),
                order.getId(),
                order.getStore().getOwnerId()
        );
        return notification;
    }

    // 고객에게 보여줄 픽업 하루 전 알림 내역을 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createPickupReminderDayBeforeNotification(Long orderId) {
        Order order = getOrderWithItems(orderId);

        Notification notification = notificationRepository.save(Notification.create(
                order.getMemberId(),
                NotificationType.PICKUP_REMINDER_DAY_BEFORE,
                "픽업 하루 전 알림",
                order.getStore().getStoreName() + " 주문 픽업이 내일 예정되어 있습니다.",
                order.getStore().getStoreName(),
                menuSummary(order.getOrderItems()),
                order.getPickupDate(),
                order.getPickupTime(),
                NotificationReferenceType.ORDER,
                order.getId()
        ));
        log.info(
                "Pickup reminder notification created. notificationId={}, orderId={}, memberId={}",
                notification.getId(),
                order.getId(),
                order.getMemberId()
        );
        return notification;
    }

    // 사업자에게 보여줄 주문 수락 마감 알림 내역을 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createOrderAcceptDeadlineNotification(Long orderId, NotificationType notificationType) {
        Order order = getOrderWithItems(orderId);
        validateOrderAcceptDeadlineType(notificationType);

        Notification notification = notificationRepository.save(Notification.create(
                order.getStore().getOwnerId(),
                notificationType,
                orderAcceptDeadlineTitle(notificationType),
                order.getStore().getStoreName() + " 주문 수락 마감이 " + orderAcceptDeadlineRemainingText(notificationType) + " 남았습니다.",
                order.getStore().getStoreName(),
                menuSummary(order.getOrderItems()),
                order.getPickupDate(),
                order.getPickupTime(),
                NotificationReferenceType.ORDER,
                order.getId()
        ));
        log.info(
                "Order accept deadline notification created. notificationId={}, orderId={}, ownerId={}, type={}",
                notification.getId(),
                order.getId(),
                order.getStore().getOwnerId(),
                notificationType
        );
        return notification;
    }

    private Order getOrderWithItems(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));
    }

    private NotificationType toCustomerOrderStatusNotificationType(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case ACCEPTED -> NotificationType.ORDER_ACCEPTED;
            case REJECTED -> NotificationType.ORDER_REJECTED;
            default -> throw new IllegalArgumentException("Unsupported order status for notification: " + orderStatus);
        };
    }

    private String customerTitle(NotificationType notificationType) {
        return switch (notificationType) {
            case ORDER_ACCEPTED -> "승인 완료";
            case ORDER_REJECTED -> "승인 거절";
            default -> "주문 상태가 변경되었습니다";
        };
    }

    private String customerBody(NotificationType notificationType, String storeName) {
        return switch (notificationType) {
            case ORDER_ACCEPTED -> storeName + "에서 주문을 승인했습니다.";
            case ORDER_REJECTED -> storeName + "에서 주문을 거절했습니다. 결제 금액은 환불 처리됩니다.";
            default -> storeName + " 주문 상태가 변경되었습니다.";
        };
    }

    private void validateOrderAcceptDeadlineType(NotificationType notificationType) {
        if (notificationType != NotificationType.ORDER_ACCEPT_DEADLINE_12H
                && notificationType != NotificationType.ORDER_ACCEPT_DEADLINE_1H) {
            throw new IllegalArgumentException("Unsupported order accept deadline notification type: " + notificationType);
        }
    }

    private String orderAcceptDeadlineTitle(NotificationType notificationType) {
        return switch (notificationType) {
            case ORDER_ACCEPT_DEADLINE_12H -> "수락 마감 12시간 전";
            case ORDER_ACCEPT_DEADLINE_1H -> "수락 마감 1시간 전";
            default -> throw new IllegalArgumentException("Unsupported order accept deadline notification type: " + notificationType);
        };
    }

    private String orderAcceptDeadlineRemainingText(NotificationType notificationType) {
        return switch (notificationType) {
            case ORDER_ACCEPT_DEADLINE_12H -> "12시간";
            case ORDER_ACCEPT_DEADLINE_1H -> "1시간";
            default -> throw new IllegalArgumentException("Unsupported order accept deadline notification type: " + notificationType);
        };
    }

    private String menuSummary(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return null;
        }

        List<OrderItem> sortedItems = orderItems.stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .toList();

        String firstMenuName = sortedItems.get(0).getMenuName();
        int remainingCount = sortedItems.size() - 1;
        if (remainingCount <= 0) {
            return firstMenuName;
        }

        return firstMenuName + " 외 " + remainingCount + "개";
    }
}
