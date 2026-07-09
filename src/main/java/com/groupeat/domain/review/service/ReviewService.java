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
import com.groupeat.domain.review.dto.response.ReviewSummaryResponse;
import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.entity.ReviewImage;
import com.groupeat.domain.review.enums.ReviewSortType;
import com.groupeat.domain.review.exception.ReviewErrorStatus;
import com.groupeat.domain.review.repository.ReviewImageRepository;
import com.groupeat.domain.review.repository.ReviewQueryRepository;
import com.groupeat.domain.review.repository.ReviewRepository;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreRepository;
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
    private final StoreRepository storeRepository;

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

        // 리뷰에 달린 이미지 삭제
        reviewImageRepository.deleteByReviewId(reviewId);

        reviewRepository.delete(review);
    }

    // 특정 가게의 리뷰 목록 조회
    public ReviewListResponse getStoreReviews(Long storeId, Long lastReviewId, ReviewSortType sortType, int size) {

        // 가게 정보 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        Integer lastRating = null;
        if (lastReviewId != null && sortType != ReviewSortType.LATEST) {
            Review lastReview = reviewRepository.findById(lastReviewId).orElse(null);
            if (lastReview != null) {
                lastRating = lastReview.getRating();
            }
        }

        List<Review> reviews = reviewQueryRepository.findStoreReviewsByCursor(storeId, lastReviewId, lastRating, sortType, size + 1);

        return createPaginatedResponse(store.getStoreName(), reviews, size);
    }

    // 내가 작성한 리뷰 목록 조회
    public ReviewListResponse getMyReviews(Long memberId, Long lastReviewId, int size) {
        List<Review> reviews = reviewQueryRepository.findMyReviewsByCursor(memberId, lastReviewId, size + 1);
        return createPaginatedResponse(null, reviews, size);
    }

    // 커서 페이징 계산 및 데이터 조립
    private ReviewListResponse createPaginatedResponse(String storeName, List<Review> reviews, int size) {
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

        return reviewConverter.toReviewListResponse(storeName, dtoList, hasNext, nextCursor);
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

    public ReviewSummaryResponse getReviewSummary(Long storeId) {
        Store store = storeRepository.findActiveStoreById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        // 해당 가게의 모든 별점 가져오기
        List<Integer> ratings = reviewRepository.findRatingsByStoreId(storeId);

        int totalReviewCount = ratings.size();
        double averageRating = totalReviewCount == 0 ? 0.0 :
                Math.round(ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0) * 10) / 10.0;

        return ReviewSummaryResponse.builder()
                .storeName(store.getStoreName())
                .averageRating(averageRating)
                .totalReviewCount(totalReviewCount)
                .rating5Count((int) ratings.stream().filter(r -> r == 5).count())
                .rating4Count((int) ratings.stream().filter(r -> r == 4).count())
                .rating3Count((int) ratings.stream().filter(r -> r == 3).count())
                .rating2Count((int) ratings.stream().filter(r -> r == 2).count())
                .rating1Count((int) ratings.stream().filter(r -> r == 1).count())
                .build();
    }
}
