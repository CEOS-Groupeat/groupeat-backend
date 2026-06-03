package com.groupeat.domain.settlement.entity;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.settlement.enums.SettlementStatus;
import com.groupeat.domain.settlement.enums.SettlementType;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "settlements")
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_pk", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "order_amount", nullable = false)
    private Integer orderAmount;

    @Column(name = "platform_fee_amount", nullable = false)
    private Integer platformFeeAmount;

    @Column(name = "payout_amount", nullable = false)
    private Integer payoutAmount;

    @Column(name = "charge_amount", nullable = false)
    private Integer chargeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_type", nullable = false)
    private SettlementType settlementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    @Builder.Default
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public static Settlement payout(
            Order order,
            Integer orderAmount,
            Integer platformFeeAmount
    ) {
        Store store = order.getStore();

        return Settlement.builder()
                .order(order)
                .store(store)
                .ownerId(store.getOwnerId())
                .orderAmount(orderAmount)
                .platformFeeAmount(platformFeeAmount)
                .payoutAmount(orderAmount - platformFeeAmount) // 전체 금액 - 수수료 금액
                .chargeAmount(0)
                .settlementType(SettlementType.PAYOUT)
                .build();
    }

    public static Settlement feeCharge(
            Order order,
            Integer orderAmount,
            Integer platformFeeAmount
    ) {
        Store store = order.getStore();

        return Settlement.builder()
                .order(order)
                .store(store)
                .ownerId(store.getOwnerId())
                .orderAmount(orderAmount)
                .platformFeeAmount(platformFeeAmount)
                .payoutAmount(0)
                .chargeAmount(platformFeeAmount) // 수수료 금액만 청구
                .settlementType(SettlementType.FEE_CHARGE)
                .build();
    }

    public void complete(LocalDateTime settledAt) {
        this.settlementStatus = SettlementStatus.COMPLETED;
        this.settledAt = settledAt;
    }

    public void cancel() {
        this.settlementStatus = SettlementStatus.CANCELED;
        this.settledAt = null;
    }
}
