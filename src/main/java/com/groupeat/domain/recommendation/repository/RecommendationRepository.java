package com.groupeat.domain.recommendation.repository;

import com.groupeat.domain.store.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

    private final JPAQueryFactory queryFactory;


    //만족도(별점) 높은 가게 추천
    public  List<Store> findTopRatedStores(int limit) {
        return queryFactory
                .selectFrom(store)
                .where(
                        store.deletedAt.isNull()
                )
                .orderBy(
                        store.reviewRating.desc(),
                        store.storeName.asc()
                )
                .limit(limit)
                .fetch();
    }

    // 할인율 높은 가게 추천
    public List<Store> findHighDiscountStores(int limit) {
        return queryFactory
                .selectFrom(store)
                .where(
                        store.deletedAt.isNull(),
                        store.discountRate.isNotNull(),
                        store.discountRate.gt(0)
                )
                .orderBy(
                        store.discountRate.desc(),
                        store.storeName.asc()
                )
                .limit(limit)
                .fetch();
    }
}