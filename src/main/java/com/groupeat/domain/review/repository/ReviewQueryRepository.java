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

    public List<Review> findStoreReviewsByCursor(Long storeId, Long lastReviewId, Integer lastRating, ReviewSortType sortType, int limit) {
        return queryFactory
                .selectFrom(review)
                .join(review.member, member).fetchJoin()
                .join(review.store, store).fetchJoin()
                .join(review.order, order).fetchJoin()
                .where(
                        review.store.id.eq(storeId),
                        cursorCondition(lastReviewId, lastRating, sortType)
                )
                .orderBy(getSortSpecifiers(sortType))
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

    private BooleanExpression cursorCondition(Long lastReviewId, Integer lastRating, ReviewSortType sortType) {
        if (lastReviewId == null) return null;

        // 별점순 정렬일 때는 동점자 처리를 위해 별점과 ID를 함께 비교
        if (sortType == ReviewSortType.HIGHEST_RATING && lastRating != null) {
            return review.rating.lt(lastRating)
                    .or(review.rating.eq(lastRating).and(review.id.lt(lastReviewId)));
        }
        if (sortType == ReviewSortType.LOWEST_RATING && lastRating != null) {
            return review.rating.gt(lastRating)
                    .or(review.rating.eq(lastRating).and(review.id.lt(lastReviewId)));
        }

        // 기본 최신순
        return review.id.lt(lastReviewId);
    }

    private OrderSpecifier<?>[] getSortSpecifiers(ReviewSortType sortType) {
        if (sortType == ReviewSortType.HIGHEST_RATING) {
            return new OrderSpecifier[]{review.rating.desc(), review.id.desc()};
        }
        if (sortType == ReviewSortType.LOWEST_RATING) {
            return new OrderSpecifier[]{review.rating.asc(), review.id.desc()};
        }
        // 기본 최신순
        return new OrderSpecifier[]{review.id.desc()};
    }
}
