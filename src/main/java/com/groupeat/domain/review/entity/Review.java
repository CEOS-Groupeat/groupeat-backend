package com.groupeat.domain.review.entity;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.review.enums.EventType;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private Integer headcount;

    @Column(nullable = false)
    private Integer perPersonBudget;

    @Column(columnDefinition = "TEXT")
    private String content;
}