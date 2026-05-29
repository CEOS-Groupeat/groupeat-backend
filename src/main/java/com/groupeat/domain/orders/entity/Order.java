package com.groupeat.domain.orders.entity;

import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.PaymentMethod;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_pk")
    private Long id; // DB 내부 PK

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId; // Toss Payments용 커스텀 ID

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "total_original_price", nullable = false)
    private Integer totalOriginalPrice;

    @Column(name = "total_discount_amount", nullable = false)
    private Integer totalDiscountAmount;

    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount; // 실제 결제 요청 금액

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "requests", columnDefinition = "TEXT")
    private String requests;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Column(name = "pickup_time", nullable = false)
    private LocalTime pickupTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void markPaid() {
        this.orderStatus = OrderStatus.PAID;
    }
}
