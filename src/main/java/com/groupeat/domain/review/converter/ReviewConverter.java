package com.groupeat.domain.review.converter;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.review.dto.request.ReviewCreateRequest;
import com.groupeat.domain.review.dto.response.ReviewCreateResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import com.groupeat.domain.review.entity.ReviewMenuRating;
import com.groupeat.domain.store.entity.Store;
import org.springframework.stereotype.Component;

@Component
public class ReviewConverter {

    public Review toReview(Member member, Store store, Order order, ReviewCreateRequest request) {
        return Review.builder()
                .member(member)
                .store(store)
                .order(order)
                .eventType(request.eventType())
                .headcount(request.headcount())
                .perPersonBudget(request.perPersonBudget())
                .content(request.content())
                .build();
    }

    public ReviewImage toReviewImage(Review review, String imageUrl) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .build();
    }

    public ReviewMenuRating toReviewMenuRating(Review review, OrderItem orderItem, Integer rating) {
        return ReviewMenuRating.builder()
                .review(review)
                .orderItem(orderItem)
                .rating(rating)
                .build();
    }

    public ReviewCreateResponse toReviewCreateResponse(Review review) {
        return ReviewCreateResponse.builder()
                .reviewId(review.getId())
                .createdAtDate(review.getCreatedAt().toLocalDate())
                .createdAtTime(review.getCreatedAt().toLocalTime())
                .build();
    }
}
