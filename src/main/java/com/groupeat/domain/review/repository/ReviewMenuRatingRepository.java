package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.ReviewMenuRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewMenuRatingRepository extends JpaRepository<ReviewMenuRating, Long> {
}
