package com.groupeat.domain.notification.enums;

public enum NotificationType {
    // 고객 : 주문 요청 승인 완료
    ORDER_ACCEPTED,

    // 고객 : 주문 요청 거절
    ORDER_REJECTED,

    // 고객 : 픽업 하루 전 알림
    PICKUP_REMINDER_DAY_BEFORE,

    // 사업자 : 신규 주문 요청 접수
    NEW_ORDER_REQUEST,

    // 사업자 : 주문 수락 마감 12시간 전 알림
    ORDER_ACCEPT_DEADLINE_12H,

    // 사업자 : 주문 수락 마감 1시간 전 알림
    ORDER_ACCEPT_DEADLINE_1H
}
