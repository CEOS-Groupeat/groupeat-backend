package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.ReviewMenuRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewMenuRatingRepository extends JpaRepository<ReviewMenuRating, Long> {
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM ReviewMenuRating r WHERE r.review.id = :reviewId")
    Double findAverageRatingByReviewId(@Param("reviewId") Long reviewId);
}
