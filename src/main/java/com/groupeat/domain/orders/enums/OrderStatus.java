package com.groupeat.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("주문 대기(예약금 결제 대기)"),
    PAID("결제 완료(사장님 수락 대기)"),
    ACCEPTED("주문 수락"),
    COMPLETED("픽업 완료"),
    REJECTED("거절"),
    CANCELLED("주문 취소");

    private final String description;
}
