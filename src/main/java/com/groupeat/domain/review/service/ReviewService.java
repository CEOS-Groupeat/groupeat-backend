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
import com.groupeat.domain.review.entity.ReviewMenuRating;
import com.groupeat.domain.review.exception.ReviewErrorStatus;
import com.groupeat.domain.review.repository.ReviewImageRepository;
import com.groupeat.domain.review.repository.ReviewMenuRatingRepository;
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
    private final ReviewMenuRatingRepository reviewMenuRatingRepository;
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

        // 메뉴별 별점 저장 (올바른 주문 항목인지 2차 검증 포함)
        List<ReviewMenuRating> menuRatings = request.menuRatings().stream()
                .map(ratingDto -> {
                    OrderItem orderItem = orderItemRepository.findById(ratingDto.orderItemId())
                            .orElseThrow(() -> new GeneralException(ReviewErrorStatus.ORDER_ITEM_NOT_FOUND));

                    if (!orderItem.getOrder().getId().equals(order.getId())) {
                        throw new GeneralException(ReviewErrorStatus.INVALID_MENU_RATING);
                    }
                    return reviewConverter.toReviewMenuRating(review, orderItem, ratingDto.rating());
                }).toList();

        reviewMenuRatingRepository.saveAll(menuRatings);

        // 가게의 총 별점 및 리뷰 개수 업데이트
        updateStoreReviewStats(store, request.menuRatings());

        return reviewConverter.toReviewCreateResponse(review);
    }

    private void updateStoreReviewStats(Store store, List<ReviewCreateRequest.MenuRatingDTO> menuRatings) {
        // 리뷰 평균 계산
        double currentReviewAverage = menuRatings.stream()
                .mapToInt(ReviewCreateRequest.MenuRatingDTO::rating)
                .average()
                .orElse(0.0);

        store.updateReviewStats(currentReviewAverage);
    }

    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {

        Member requestMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        // 리뷰 조회 및 존재 여부 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ReviewErrorStatus.REVIEW_NOT_FOUND));

        // 권한 검증
        boolean isAuthor = review.getMember().getId().equals(memberId);
        boolean isAdmin = requestMember.isAdmin();

        // 본인도 아니고 관리자도 아니라면 예외 발생
        if (!isAuthor && !isAdmin) {
            throw new GeneralException(ReviewErrorStatus.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 가게 별점 롤백 처리
        Store store = review.getStore();

        Double oldReviewAverage = reviewMenuRatingRepository.findAverageRatingByReviewId(reviewId);
        store.removeReviewStats(oldReviewAverage);
        
        reviewRepository.delete(review);
    }
}
