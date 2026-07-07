package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewIdIn(List<Long> reviewIds);

    // 특정 리뷰의 이미지들을 일괄 삭제
    void deleteByReviewId(Long reviewId);
}
