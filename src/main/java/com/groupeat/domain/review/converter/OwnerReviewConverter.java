package com.groupeat.domain.review.converter;

import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.review.dto.response.OwnerReplyCreateResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewListResponse.OwnerReviewCardDTO;
import com.groupeat.domain.review.dto.response.OwnerReviewSummaryResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OwnerReviewConverter {

    public OwnerReviewCardDTO toOwnerReviewCardDTO(Review review, List<ReviewImage> images, List<OrderItem> orderItems) {

        List<String> imageUrls = images.stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        List<String> orderedMenuNames = orderItems.stream()
                .map(OrderItem::getMenuName)
                .toList();

        return OwnerReviewCardDTO.builder()
                .reviewId(review.getId())
                .authorNickname(maskNickname(review.getMember().getName()))
                .rating(review.getRating())
                .eventType(review.getEventType())
                .headcount(review.getHeadcount())
                .perPersonBudget(review.getPerPersonBudget())
                .content(review.getContent())
                .createdAt(review.getCreatedAt().toLocalDate())
                .imageUrls(imageUrls)
                .orderedMenuNames(orderedMenuNames)
                .ownerReplyContent(review.getOwnerReplyContent())
                .repliedAt(review.getRepliedAt() != null ? review.getRepliedAt().toLocalDate() : null)
                .build();
    }

    public OwnerReviewSummaryResponse toSummaryResponse(String storeName, List<Integer> ratings) {
        int totalReviewCount = ratings.size();

        if (totalReviewCount == 0) {
            return OwnerReviewSummaryResponse.builder()
                    .storeName(storeName)
                    .averageRating(0.0)
                    .totalReviewCount(0)
                    .build();
        }

        double averageRating = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        averageRating = Math.round(averageRating * 10) / 10.0;

        return OwnerReviewSummaryResponse.builder()
                .storeName(storeName)
                .averageRating(averageRating)
                .totalReviewCount(totalReviewCount)
                .rating5Count((int) ratings.stream().filter(r -> r == 5).count())
                .rating4Count((int) ratings.stream().filter(r -> r == 4).count())
                .rating3Count((int) ratings.stream().filter(r -> r == 3).count())
                .rating2Count((int) ratings.stream().filter(r -> r == 2).count())
                .rating1Count((int) ratings.stream().filter(r -> r == 1).count())
                .build();
    }

    private String maskNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return nickname;
        }
        if (nickname.length() == 1) {
            return nickname;
        }
        String firstLetter = nickname.substring(0, 1);
        String maskedPart = "*".repeat(nickname.length() - 1);
        return firstLetter + maskedPart;
    }

    public OwnerReplyCreateResponse toOwnerReplyCreateResponse(Review review) {
        return OwnerReplyCreateResponse.builder()
                .reviewId(review.getId())
                .repliedAtDate(review.getRepliedAt().toLocalDate())
                .repliedAtTime(review.getRepliedAt().toLocalTime())
                .build();
    }
}