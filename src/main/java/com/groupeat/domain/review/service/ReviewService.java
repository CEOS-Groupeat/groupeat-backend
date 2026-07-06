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
import com.groupeat.domain.review.dto.response.ReviewListResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import com.groupeat.domain.review.exception.ReviewErrorStatus;
import com.groupeat.domain.review.repository.ReviewImageRepository;
import com.groupeat.domain.review.repository.ReviewQueryRepository;
import com.groupeat.domain.review.repository.ReviewRepository;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewQueryRepository reviewQueryRepository;
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

    // 특정 가게의 리뷰 목록 조회
    public ReviewListResponse getStoreReviews(Long storeId, Long lastReviewId, int size) {
        List<Review> reviews = reviewQueryRepository.findStoreReviewsByCursor(storeId, lastReviewId, size + 1);
        return createPaginatedResponse(reviews, size);
    }

    // 내가 작성한 리뷰 목록 조회
    public ReviewListResponse getMyReviews(Long memberId, Long lastReviewId, int size) {
        List<Review> reviews = reviewQueryRepository.findMyReviewsByCursor(memberId, lastReviewId, size + 1);
        return createPaginatedResponse(reviews, size);
    }

    // 커서 페이징 계산 및 데이터 조립
    private ReviewListResponse createPaginatedResponse(List<Review> reviews, int size) {
        boolean hasNext = false;
        Long nextCursor = null;

        if (reviews.size() > size) {
            hasNext = true;
            reviews.remove(size);
        }

        if (!reviews.isEmpty()) {
            nextCursor = reviews.getLast().getId();
        }

        List<ReviewListResponse.ReviewDetailDTO> dtoList = assembleReviews(reviews);
        return new ReviewListResponse(dtoList, hasNext, nextCursor);
    }

    private List<ReviewListResponse.ReviewDetailDTO> assembleReviews(List<Review> reviews) {
        if (reviews.isEmpty()) return List.of();

        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        List<Long> orderIds = reviews.stream().map(r -> r.getOrder().getId()).toList();

        // 자식 데이터 한 방에 가져와서 메모리에서 매핑
        Map<Long, List<ReviewImage>> imagesMap = reviewImageRepository.findByReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(image -> image.getReview().getId()));

        Map<Long, List<OrderItem>> orderItemsMap = orderItemRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        return reviews.stream().map(review -> {
            List<ReviewImage> images = imagesMap.getOrDefault(review.getId(), List.of());
            List<OrderItem> orderItems = orderItemsMap.getOrDefault(review.getOrder().getId(), List.of());
            return reviewConverter.toReviewDetailDTO(review, images, orderItems);
        }).toList();
    }
}
