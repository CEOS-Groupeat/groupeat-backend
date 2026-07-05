package com.groupeat.domain.review.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.review.converter.ReviewConverter;
import com.groupeat.domain.review.dto.request.ReviewCreateRequest;
import com.groupeat.domain.review.dto.response.ReviewCreateResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import com.groupeat.domain.review.exception.ReviewErrorStatus;
import com.groupeat.domain.review.repository.ReviewImageRepository;
import com.groupeat.domain.review.repository.ReviewRepository;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final ReviewConverter reviewConverter;
    private final MemberRepository memberRepository;

    @Transactional
    public ReviewCreateResponse createReview(Long memberId, ReviewCreateRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new GeneralException(ReviewErrorStatus.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new GeneralException(ReviewErrorStatus.UNAUTHORIZED_REVIEW_ACCESS);
        }
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new GeneralException(ReviewErrorStatus.ORDER_NOT_COMPLETED);
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new GeneralException(ReviewErrorStatus.REVIEW_ALREADY_EXISTS);
        }

        Store store = order.getStore();

        // 리뷰 기본 정보 저장
        Review review = reviewConverter.toReview(member, store, order, request);
        reviewRepository.save(review);

        // 리뷰 이미지 저장
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<ReviewImage> images = request.imageUrls().stream()
                    .map(url -> reviewConverter.toReviewImage(review, url))
                    .toList();
            reviewImageRepository.saveAll(images);
        }

        // 가게의 총 별점 및 리뷰 개수 업데이트
        store.updateReviewStats(request.rating());

        return reviewConverter.toReviewCreateResponse(review);
    }


    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {

        Member requestMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ReviewErrorStatus.REVIEW_NOT_FOUND));

        boolean isAuthor = review.getMember().getId().equals(memberId);
        boolean isAdmin = requestMember.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new GeneralException(ReviewErrorStatus.UNAUTHORIZED_REVIEW_ACCESS);
        }

        Store store = review.getStore();

        store.removeReviewStats(review.getRating());

        reviewRepository.delete(review);
    }
}
