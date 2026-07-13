package com.groupeat.domain.notification.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.notification.dto.FcmSendRequest;
import com.groupeat.domain.notification.event.OrderStatusNotificationEvent;
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
    private final FcmMessageSender fcmMessageSender;

    // 주문 상태 변경 트랜잭션 커밋 이후 FCM 알림 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderStatusNotification(OrderStatusNotificationEvent event) {
        try {
            Member member = memberRepository.findById(event.memberId()).orElse(null);
            if (member == null || !member.isOrderStatusNotificationAgreed()) {
                return;
            }

            if (!isNotificationTargetStatus(event.orderStatus())) {
                return;
            }

            fcmMessageSender.sendToMember(new FcmSendRequest(
                    event.memberId(),
                    title(event.orderStatus()),
                    body(event.orderStatus(), event.storeName()),
                    data(event)
            ));
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

    private String title(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case ACCEPTED -> "주문이 승인되었습니다";
            case REJECTED -> "주문이 거절되었습니다";
            default -> "주문 상태가 변경되었습니다";
        };
    }

    private String body(OrderStatus orderStatus, String storeName) {
        return switch (orderStatus) {
            case ACCEPTED -> storeName + "에서 주문을 승인했습니다.";
            case REJECTED -> storeName + "에서 주문을 거절했습니다. 결제 금액은 환불 처리됩니다.";
            default -> storeName + " 주문 상태가 변경되었습니다.";
        };
    }

    private Map<String, String> data(OrderStatusNotificationEvent event) {
        return Map.of(
                "type", "ORDER_STATUS_CHANGED",
                "orderId", String.valueOf(event.orderId()),
                "orderStatus", event.orderStatus().name()
        );
    }
}
