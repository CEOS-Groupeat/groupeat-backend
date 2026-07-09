package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 이미 리뷰가 작성된 주문인지 확인하기 위한 메서드
    boolean existsByOrderId(Long orderId);

    // 넘겨받은 orderIds 중 리뷰가 존재하는 orderId만 추출
    @Query("SELECT r.order.id FROM Review r WHERE r.order.id IN :orderIds")
    Set<Long> findReviewedOrderIds(@Param("orderIds") List<Long> orderIds);

    @Query("SELECT r.rating FROM Review r WHERE r.store.id = :storeId")
    List<Integer> findRatingsByStoreId(@Param("storeId") Long storeId);
}