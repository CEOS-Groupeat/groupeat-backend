package com.groupeat.domain.review.converter;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.review.dto.request.ReviewCreateRequest;
import com.groupeat.domain.review.dto.response.ReviewCreateResponse;
import com.groupeat.domain.review.dto.response.ReviewListResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import com.groupeat.domain.store.entity.Store;
import org.springframework.stereotype.Component;

import java.util.List;

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
                .rating(request.rating())
                .content(request.content())
                .build();
    }

    public ReviewImage toReviewImage(Review review, String imageUrl) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .build();
    }


    public ReviewCreateResponse toReviewCreateResponse(Review review) {
        return ReviewCreateResponse.builder()
                .reviewId(review.getId())
                .createdAtDate(review.getCreatedAt().toLocalDate())
                .createdAtTime(review.getCreatedAt().toLocalTime())
                .build();
    }

    public ReviewListResponse.ReviewDetailDTO toReviewDetailDTO(Review review, List<ReviewImage> images, List<OrderItem> orderItems) {

        // 첨부된 이미지 URL 리스트 변환
        List<String> imageUrls = images.stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        // 해당 리뷰가 달린 주문의 메뉴 이름들 추출
        List<String> orderedMenuNames = orderItems.stream()
                .map(OrderItem::getMenuName)
                .toList();

        // 사장님 답글 DTO 조립
        ReviewListResponse.OwnerReplyDTO ownerReply = null;
        if (review.getOwnerReplyContent() != null && !review.getOwnerReplyContent().isBlank()) {
            ownerReply = ReviewListResponse.OwnerReplyDTO.builder()
                    .storeName(review.getStore().getStoreName())
                    .replyContent(review.getOwnerReplyContent())
                    .repliedAt(review.getRepliedAt() != null ? review.getRepliedAt().toLocalDate() : null)
                    .build();
        }

        return ReviewListResponse.ReviewDetailDTO.builder()
                .storeId(review.getOrder().getStore().getId())
                .storeName(review.getOrder().getStore().getStoreName())
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
                .ownerReply(ownerReply)
                .build();
    }

    private String maskNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return nickname;
        }

        // 이름이 1글자인 경우 마스킹 없이 그대로 반환
        if (nickname.length() == 1) {
            return nickname;
        }

        // 첫 글자만 자르고, 나머지 길이만큼 '*' 반복
        String firstLetter = nickname.substring(0, 1);
        String maskedPart = "*".repeat(nickname.length() - 1);

        return firstLetter + maskedPart;
    }

    public ReviewListResponse toReviewListResponse(
            String storeName,
            List<ReviewListResponse.ReviewDetailDTO> reviewList,
            boolean hasNext,
            Long nextCursor
    ) {
        return ReviewListResponse.builder()
                .storeName(storeName)
                .reviewList(reviewList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
