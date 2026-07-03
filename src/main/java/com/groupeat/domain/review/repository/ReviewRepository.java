package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 이미 리뷰가 작성된 주문인지 확인하기 위한 메서드
    boolean existsByOrderId(Long orderId);
}