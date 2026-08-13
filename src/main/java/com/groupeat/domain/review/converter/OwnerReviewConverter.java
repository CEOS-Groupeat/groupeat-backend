package com.groupeat.domain.review.converter;

import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.review.dto.response.OwnerReplyCreateResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewListResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewListResponse.OwnerReviewCardDTO;
import com.groupeat.domain.review.dto.response.ReviewSummaryResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import com.groupeat.domain.review.enums.ReviewSortType;
import com.groupeat.global.dto.CursorResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    public ReviewSummaryResponse toSummaryResponse(String storeName, List<Integer> ratings) {
        int totalReviewCount = ratings.size();

        if (totalReviewCount == 0) {
            return ReviewSummaryResponse.builder()
                    .storeName(storeName)
                    .averageRating(0.0)
                    .totalReviewCount(0)
                    .build();
        }

        double averageRating = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        averageRating = Math.round(averageRating * 10) / 10.0;

        return ReviewSummaryResponse.builder()
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

    public OwnerReviewListResponse toEmptyOwnerReviewListResponse() {
        return OwnerReviewListResponse.builder()
                .reviewList(List.of())
                .hasNext(false)
                .nextCursor(null)
                .nextRating(null)
                .build();
    }

    public OwnerReviewListResponse toOwnerReviewListResponse(
            CursorResponse<Review> cursorResponse,
            Map<Long, List<ReviewImage>> imagesMap,
            Map<Long, List<OrderItem>> orderItemsMap,
            ReviewSortType sortType
    ) {
        // 엔티티 -> DTO 변환
        CursorResponse<OwnerReviewCardDTO> dtoCursorResponse = cursorResponse.map(review -> {
            List<ReviewImage> images = imagesMap.getOrDefault(review.getId(), List.of());
            List<OrderItem> orderItems = orderItemsMap.getOrDefault(review.getOrder().getId(), List.of());
            return toOwnerReviewCardDTO(review, images, orderItems);
        });

        Integer nextRating = null;
        if (sortType != null && sortType != ReviewSortType.LATEST && !cursorResponse.content().isEmpty()) {
            nextRating = cursorResponse.content().getLast().getRating();
        }

        return OwnerReviewListResponse.builder()
                .reviewList(dtoCursorResponse.content())
                .hasNext(dtoCursorResponse.hasNext())
                .nextCursor(dtoCursorResponse.nextCursor())
                .nextRating(nextRating)
                .build();
    }
}