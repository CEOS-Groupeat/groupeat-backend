package com.groupeat.domain.payment.entity;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.payment.enums.PaymentProvider;
import com.groupeat.domain.payment.enums.PaymentStatus;
import com.groupeat.domain.payment.enums.PaymentType;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_pk")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_pk", nullable = false)
    private Order order;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "payment_key", unique = true)
    private String paymentKey;

    // 선결제 또는 현장결제 정책 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    // 현재는 토스페이먼츠만 사용
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false)
    private PaymentProvider paymentProvider;

    @Column(name = "total_order_amount", nullable = false)
    private Integer totalOrderAmount;

    // 실제 PG 승인 요청 금액
    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    // 현장결제 선택 시 현장에서 추가로 받을 금액
    @Column(name = "remaining_amount", nullable = false)
    private Integer remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    @Column(name = "method")
    private String method;

    // 토스 결제/취소 거래를 추적할 때 사용하는 마지막 거래 키
    @Column(name = "last_transaction_key")
    private String lastTransactionKey;

    // 토스 응답 기준 부분 취소 가능 여부
    @Column(name = "is_partial_cancelable")
    private Boolean isPartialCancelable;

    // 결제 증빙용 영수증 URL
    @Column(name = "receipt_url")
    private String receiptUrl;

    // 카드 결제 승인번호
    @Column(name = "card_approve_no")
    private String cardApproveNo;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "refunded_amount")
    private Integer refundedAmount;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "failure_message", columnDefinition = "TEXT")
    private String failureMessage;

    public static Payment ready(
            Order order,
            Long memberId,
            PaymentType paymentType,
            Integer totalOrderAmount,
            Integer paidAmount
    ) {
        return Payment.builder()
                .order(order)
                .orderId(order.getOrderId())
                .memberId(memberId)
                .paymentType(paymentType)
                .paymentProvider(PaymentProvider.TOSS)
                .totalOrderAmount(totalOrderAmount)
                .paidAmount(paidAmount)
                .remainingAmount(calculateRemainingAmount(paymentType, totalOrderAmount, paidAmount))
                .build();
    }

    private static Integer calculateRemainingAmount(
            PaymentType paymentType,
            Integer totalOrderAmount,
            Integer paidAmount
    ) {
        // 현장결제는 현재 PG 결제 없이 매장에서 전체 금액을 결제한다.
        // 예약금 PG 결제 도입 후에도 현장 결제 대상 금액은 전체 주문 금액으로 유지한다.
        if (paymentType == PaymentType.ON_SITE) {
            return totalOrderAmount;
        }

        return totalOrderAmount - paidAmount;
    }

    // 결제 승인 진행 중
    public void markInProgress(String paymentKey) {
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.IN_PROGRESS;
    }

    // 결제 승인 완료
    public void approve(
            String paymentKey,
            String method,
            LocalDateTime approvedAt,
            String lastTransactionKey,
            Boolean isPartialCancelable,
            String receiptUrl,
            String cardApproveNo
    ) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.approvedAt = approvedAt;
        this.lastTransactionKey = lastTransactionKey;
        this.isPartialCancelable = isPartialCancelable;
        this.receiptUrl = receiptUrl;
        this.cardApproveNo = cardApproveNo;
        this.paymentStatus = PaymentStatus.DONE;
    }

    // 결제 실패
    public void fail(String failureCode, String failureMessage) {
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.paymentStatus = PaymentStatus.FAILED;
    }

    // 결제 취소
    public void cancel(Integer refundedAmount, LocalDateTime canceledAt) {
        this.refundedAmount = refundedAmount;
        this.canceledAt = canceledAt;
        this.paymentStatus = PaymentStatus.CANCELED;
    }
}
