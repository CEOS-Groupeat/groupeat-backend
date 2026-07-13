package com.groupeat.domain.review.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.review.converter.OwnerReviewConverter;
import com.groupeat.domain.review.dto.request.OwnerReplyCreateRequest;
import com.groupeat.domain.review.dto.response.OwnerReplyCreateResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewListResponse;
import com.groupeat.domain.review.dto.response.OwnerReviewListResponse.OwnerReviewCardDTO;
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
import com.groupeat.global.dto.CursorResponse;
import com.groupeat.global.exception.GeneralException;
import com.groupeat.global.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final OwnerReviewConverter ownerReviewConverter;
    private final MemberRepository memberRepository;


    public ReviewSummaryResponse getReviewSummary(Long ownerId) {
        Store store = storeRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        // 사장님 본인의 가게가 맞는지 확인
        validateStoreOwner(store, ownerId);

        List<Integer> ratings = reviewRepository.findRatingsByStoreId(store.getId());

        return ownerReviewConverter.toSummaryResponse(store.getStoreName(), ratings);
    }

    public OwnerReviewListResponse getStoreReviews(
            Long ownerId,
            Long lastReviewId,
            Integer lastRating,
            ReviewSortType sortType,
            int size
    ) {
        Store store = storeRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        validateStoreOwner(store, ownerId);

        List<Review> reviews = reviewQueryRepository.findOwnerReviewsByCursor(
                store.getId(), lastReviewId, lastRating, sortType, size + 1
        );

        CursorResponse<Review> cursorResponse = CursorUtils.getCursorResponse(reviews, size, Review::getId);

        if (cursorResponse.content().isEmpty()) {
            return ownerReviewConverter.toEmptyOwnerReviewListResponse();
        }

        List<Long> reviewIds = cursorResponse.content().stream().map(Review::getId).toList();
        List<Long> orderIds = cursorResponse.content().stream().map(r -> r.getOrder().getId()).toList();

        Map<Long, List<ReviewImage>> imagesMap = reviewImageRepository.findByReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(image -> image.getReview().getId()));

        Map<Long, List<OrderItem>> orderItemsMap = orderItemRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        return ownerReviewConverter.toOwnerReviewListResponse(cursorResponse, imagesMap, orderItemsMap, sortType);
    }

    // 답글 작성
    @Transactional
    public OwnerReplyCreateResponse createReply(Long ownerId, Long reviewId, OwnerReplyCreateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ReviewErrorStatus.REVIEW_NOT_FOUND));

        validateStoreOwner(review.getStore(), ownerId);

        review.writeOwnerReply(request.replyContent());

        return ownerReviewConverter.toOwnerReplyCreateResponse(review);
    }

    // 사장님 권한 검증 로직
    private void validateStoreOwner(Store store, Long ownerId) {
        Member member = memberRepository.findById(ownerId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }

        if (member.getMemberType() != MemberType.BUSINESS) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
        }

        if (!store.getOwnerId().equals(ownerId)) {
            throw new GeneralException(ReviewErrorStatus.UNAUTHORIZED_REVIEW_ACCESS);
        }
    }
}
