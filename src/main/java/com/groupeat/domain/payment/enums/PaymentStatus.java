package com.groupeat.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 요청 생성"),
    IN_PROGRESS("결제 승인 진행 중"),
    DONE("결제 승인 완료"),
    FAILED("결제 실패"),
    CANCELED("결제 전액 취소"),
    PARTIAL_CANCELED("결제 부분 취소");

    private final String description;
}
