package com.groupeat.domain.review.repository;

import com.groupeat.domain.review.entity.Review;
import com.querydsl.core.types.EntityPath;
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
}
