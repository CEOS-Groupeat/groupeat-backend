package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.Review;
import com.groupeat.domain.review.enums.ReviewSortType;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groupeat.domain.member.entity.QMember.member;
import static com.groupeat.domain.orders.entity.QOrder.order;
import static com.groupeat.domain.review.entity.QReview.review;
import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Review> findStoreReviewsByCursor(Long storeId, Long lastReviewId, int limit) {
        return queryFactory
                .selectFrom(review)
                .join(review.member, member).fetchJoin()
                .join(review.store, store).fetchJoin()
                .join(review.order, order).fetchJoin()
                .where(
                        review.store.id.eq(storeId),
                        ltReviewId(lastReviewId)
                )
                .orderBy(review.id.desc())
                .limit(limit)
                .fetch();
    }

    public List<Review> findMyReviewsByCursor(Long memberId, Long lastReviewId, int limit) {
        return queryFactory
                .selectFrom(review)
                .join(review.member, member).fetchJoin()
                .join(review.store, store).fetchJoin()
                .join(review.order, order).fetchJoin()
                .where(
                        review.member.id.eq(memberId),
                        ltReviewId(lastReviewId)
                )
                .orderBy(review.id.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression ltReviewId(Long lastReviewId) {
        return lastReviewId != null ? review.id.lt(lastReviewId) : null;
    }

    public List<Review> findOwnerReviewsByCursor(
            Long storeId,
            Long lastReviewId,
            Integer lastRating,
            ReviewSortType sortType,
            int limit
    ) {
        return queryFactory
                .selectFrom(review)
                .join(review.member, member).fetchJoin()
                .join(review.store, store).fetchJoin()
                .join(review.order, order).fetchJoin()
                .where(
                        review.store.id.eq(storeId),
                        dynamicCursor(sortType, lastReviewId, lastRating)
                )
                .orderBy(dynamicOrderSpecifiers(sortType))
                .limit(limit)
                .fetch();
    }

    // 동적 정렬 조건 생성
    private OrderSpecifier<?>[] dynamicOrderSpecifiers(ReviewSortType sortType) {
        if (sortType == null || sortType == ReviewSortType.LATEST) {
            return new OrderSpecifier[]{ review.id.desc() };
        } else if (sortType == ReviewSortType.HIGHEST_RATING) {
            return new OrderSpecifier[]{ review.rating.desc(), review.id.desc() };
        } else { // LOWEST_RATING
            return new OrderSpecifier[]{ review.rating.asc(), review.id.desc() };
        }
    }

    // 동적 커서(WHERE) 조건 생성
    private BooleanExpression dynamicCursor(ReviewSortType sortType, Long lastReviewId, Integer lastRating) {
        if (lastReviewId == null) {
            return null;
        }

        if (sortType == null || sortType == ReviewSortType.LATEST) {
            return review.id.lt(lastReviewId);
        }

        if (lastRating == null) {
            return review.id.lt(lastReviewId);
        }

        if (sortType == ReviewSortType.HIGHEST_RATING) {
            // 별점이 더 낮거나 OR (별점은 같은데 ID가 더 작음)
            return review.rating.lt(lastRating)
                    .or(review.rating.eq(lastRating).and(review.id.lt(lastReviewId)));
        } else { // LOWEST_RATING
            // 별점이 더 높거나 OR (별점은 같은데 ID가 더 작음)
            return review.rating.gt(lastRating)
                    .or(review.rating.eq(lastRating).and(review.id.lt(lastReviewId)));
        }
    }
}
