package com.groupeat.domain.search.repository;

import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.enums.StoreSortType;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.QStoreOrderScheduleTimeRange;
import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import com.groupeat.domain.store.enums.StoreOrderScheduleTimeRangeType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.groupeat.domain.store.entity.QStoreOrderSchedule.storeOrderSchedule;
import static com.groupeat.domain.store.entity.QStoreOrderScheduleDay.storeOrderScheduleDay;
import static com.groupeat.domain.store.entity.QStoreOrderScheduleTimeRange.storeOrderScheduleTimeRange;
import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private final JPAQueryFactory queryFactory;

    // 조건에 맞는 가게 목록 조회
    public List<Store> searchStores(StoreSearchCondition condition) {
        JPAQuery<Store> query = queryFactory
                .selectFrom(store)
                .where(
                        keywordContains(condition.keyword()),
                        regionEq(condition.region()),
                        categoryEq(condition.category()),
                        budgetLessThanOrEqualTo(condition.budget())
                );

        applyScheduleJoinIfNeeded(query, condition);

        return query.orderBy(getSortOrder(condition.sortType()))
                .distinct()
                .fetch();
    }

    // 페이징 처리를 위한 전체 개수 카운트 쿼리
    public long countStores(StoreSearchCondition condition) {
        JPAQuery<Long> query = queryFactory
                .select(store.countDistinct())
                .from(store)
                .where(
                        keywordContains(condition.keyword()),
                        regionEq(condition.region()),
                        categoryEq(condition.category()),
                        budgetLessThanOrEqualTo(condition.budget())
                );

        applyScheduleJoinIfNeeded(query, condition);

        Long count = query.fetchOne();
        return count != null ? count : 0L;
    }

    // 키워드가 가게명에 포함되어 있는지 검증 (가게명 검색)
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? store.storeName.contains(keyword) : null;
    }

    // 지역 일치 여부
    private BooleanExpression regionEq(StoreRegion region) {
        return region != null ? store.region.eq(region) : null;
    }

    // 카테고리 일치 여부
    private BooleanExpression categoryEq(StoreCategory category) {
        return category != null ? store.category.eq(category) : null;
    }

    // 유저 예산 범위 안에 존재하는지 검증 (최소 가격이 예산 이하인 가게)
    private BooleanExpression budgetLessThanOrEqualTo(Integer budget) {
        return budget != null ? store.minPrice.loe(budget) : null;
    }

    private void applyScheduleJoinIfNeeded(JPAQuery<?> query, StoreSearchCondition condition) {
        if (!hasScheduleCondition(condition)) {
            return;
        }

        query.join(storeOrderSchedule)
                .on(
                        storeOrderSchedule.store.id.eq(store.id),
                        storeOrderSchedule.deletedAt.isNull(),
                        isPickupDateInSchedulePeriod(condition.pickupDate())
                )
                .join(storeOrderScheduleDay)
                .on(
                        storeOrderScheduleDay.schedule.id.eq(storeOrderSchedule.id),
                        storeOrderScheduleDay.deletedAt.isNull(),
                        storeOrderScheduleDay.available.isTrue(),
                        isPickupDayAvailable(condition.pickupDate()),
                        isLeadTimeEnough(condition.pickupDate()),
                        isQuantitySatisfied(condition.quantity())
                )
                .join(storeOrderScheduleTimeRange)
                .on(
                        storeOrderScheduleTimeRange.scheduleDay.id.eq(storeOrderScheduleDay.id),
                        storeOrderScheduleTimeRange.deletedAt.isNull(),
                        storeOrderScheduleTimeRange.type.eq(StoreOrderScheduleTimeRangeType.PICKUP),
                        isPickupTimesAvailable(condition.pickupTimes())
                );
    }

    private boolean hasScheduleCondition(StoreSearchCondition condition) {
        return condition.pickupDate() != null
                || (condition.pickupTimes() != null && !condition.pickupTimes().isEmpty())
                || condition.quantity() != null;
    }

    private BooleanExpression isPickupDateInSchedulePeriod(LocalDate requestedDate) {
        if (requestedDate == null) {
            return null;
        }

        return storeOrderSchedule.startDate.loe(requestedDate)
                .and(storeOrderSchedule.endDate.goe(requestedDate));
    }

    private BooleanExpression isPickupDayAvailable(LocalDate requestedDate) {
        if (requestedDate == null) {
            return null;
        }

        return storeOrderScheduleDay.dayOfWeek.eq(requestedDate.getDayOfWeek());
    }

    private BooleanExpression isLeadTimeEnough(LocalDate requestedDate) {
        if (requestedDate == null) {
            return null;
        }

        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), requestedDate);
        return storeOrderSchedule.minOrderDays.loe((int) daysBetween);
    }

    private BooleanExpression isPickupTimesAvailable(List<LocalTime> requestedTimes) {
        if (requestedTimes == null || requestedTimes.isEmpty()) {
            return null;
        }

        BooleanExpression result = null;

        for (LocalTime time : requestedTimes) {
            BooleanExpression timeCondition = storeOrderScheduleTimeRange.startTime.loe(time)
                    .and(storeOrderScheduleTimeRange.endTime.goe(time))
                    .and(isPickupTimeNotInBreakTime(time));

            result = (result == null) ? timeCondition : result.or(timeCondition);
        }

        return result;
    }

    private BooleanExpression isPickupTimeNotInBreakTime(LocalTime time) {
        QStoreOrderScheduleTimeRange breakTimeRange = new QStoreOrderScheduleTimeRange("breakTimeRange");
        return JPAExpressions
                .selectOne()
                .from(breakTimeRange)
                .where(
                        breakTimeRange.scheduleDay.id.eq(storeOrderScheduleDay.id),
                        breakTimeRange.deletedAt.isNull(),
                        breakTimeRange.type.eq(StoreOrderScheduleTimeRangeType.BREAK),
                        breakTimeRange.startTime.loe(time),
                        breakTimeRange.endTime.gt(time)
                )
                .notExists();
    }

    private BooleanExpression isQuantitySatisfied(Integer requestedQuantity) {
        if (requestedQuantity == null) return null;

        return storeOrderScheduleDay.minOrderQuantity.loe(requestedQuantity)
                .and(storeOrderScheduleDay.maxOrderQuantity.goe(requestedQuantity));
    }

    // 동적 정렬 조건 분기 블록
    private OrderSpecifier<?> getSortOrder(StoreSortType sortType) {
        if (sortType == null) {
            return store.storeName.asc(); // 기본 가나다순
        }

        return switch (sortType) {
            case NONE -> store.storeName.asc();               // 전체 (가나다 순)
            case DISCOUNT_DESC -> store.discountRate.desc();  // 할인율 높은 순
            case PRICE_ASC -> store.minPrice.asc();           // 가격 낮은 순
            case PRICE_DESC -> store.maxPrice.desc();         // 가격 높은 순
            case RATING_DESC -> store.reviewRating.desc();    // 별점 높은 순
            case ORDER_DESC -> store.id.desc();               // 주문 많은 순 (우선 임시로 최신순 대체)
        };
    }
}
