package com.groupeat.domain.payment.entity;

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

    // TODO : 실제로 Order 머지되면 연관관계 설정하기
    @Column(name = "order_pk")
    private Long orderPk;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "payment_key", unique = true)
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false)
    private PaymentProvider paymentProvider;

    @Column(name = "total_order_amount", nullable = false)
    private Integer totalOrderAmount;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(name = "remaining_amount", nullable = false)
    private Integer remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    @Column(name = "method")
    private String method;

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

    public void markInProgress(String paymentKey) {
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.IN_PROGRESS;
    }

    public void approve(String paymentKey, String method, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.approvedAt = approvedAt;
        this.paymentStatus = PaymentStatus.DONE;
    }

    public void fail(String failureCode, String failureMessage) {
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void cancel(Integer refundedAmount, LocalDateTime canceledAt) {
        this.refundedAmount = refundedAmount;
        this.canceledAt = canceledAt;
        this.paymentStatus = PaymentStatus.CANCELED;
    }
}
