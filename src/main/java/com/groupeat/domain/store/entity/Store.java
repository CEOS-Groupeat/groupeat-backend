package com.groupeat.domain.store.entity;

import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "store")
public class Store extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String storeName;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private StoreCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", length = 50)
    private StoreRegion region;

    @Column(length = 20)
    private String dong;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String description;

    @Column(name = "order_process", columnDefinition = "TEXT")
    private String orderProcess;

    @Builder.Default
    private Double reviewRating = 0.0;

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "pickup_open_time")
    private LocalTime pickupOpenTime;

    @Column(name = "pickup_close_time")
    private LocalTime pickupCloseTime;

    @Column(name = "closed_days")
    private String closedDays;

    @Column(name = "min_order_days")
    private Integer minOrderDays;

    @Column(name = "discount_condition_quantity")
    private Integer discountConditionQuantity;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "min_price")
    private Integer minPrice;

    @Column(name = "max_price")
    private Integer maxPrice;
}