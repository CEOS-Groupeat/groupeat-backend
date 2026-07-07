package com.groupeat.domain.store.entity;

import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    // 사업자 회원(Member)의 id
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String storeName;

    // 카카오 주소 검색 결과에서 선택한 도로명/지번 주소
    @Column(nullable = false)
    private String address;

    // 주소의 구 단위 정보, 예: 마포구
    @Column(name = "district", length = 50)
    private String district;

    // 주소의 동 단위 정보, 예: 상수동
    @Column(name = "neighborhood", length = 50)
    private String neighborhood;

    // 사용자가 직접 입력하는 층/호수 등 상세주소
    @Column(name = "detail_address")
    private String detailAddress;

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

    @Builder.Default
    private Long totalRatingScore = 0L;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "discount_condition_quantity")
    private Integer discountConditionQuantity;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "min_price")
    private Integer minPrice;

    @Column(name = "max_price")
    private Integer maxPrice;

    public void updateOwnerStoreInfo(
            String storeName,
            String imageUrl,
            String address,
            String district,
            String neighborhood,
            String detailAddress,
            StoreCategory category,
            String phoneNumber,
            String description,
            Integer discountConditionQuantity,
            Integer discountRate
    ) {
        this.storeName = storeName;
        this.imageUrl = imageUrl;
        this.address = address;
        this.district = district;
        this.neighborhood = neighborhood;
        this.detailAddress = detailAddress;
        this.category = category;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.discountConditionQuantity = discountConditionQuantity;
        this.discountRate = discountRate;
    }

    public void updateMenuPriceRange(Integer minPrice, Integer maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public void updateReviewStats(int newRating) {
        this.reviewCount++;
        this.totalRatingScore += newRating;
        this.reviewRating = (double) this.totalRatingScore / this.reviewCount;
    }

    public void removeReviewStats(int oldRating) {
        if (this.reviewCount > 0) {
            this.reviewCount--;
            this.totalRatingScore -= oldRating;
            this.reviewRating = this.reviewCount == 0 ? 0.0 : (double) this.totalRatingScore / this.reviewCount;
        }
    }
}
