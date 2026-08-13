package com.groupeat.domain.search.repository;

import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.enums.StoreSortType;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.enums.StoreCategory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.groupeat.domain.store.entity.QStore.store;
import static com.groupeat.domain.store.entity.QStoreOrderSchedule.storeOrderSchedule;
import static com.groupeat.domain.store.entity.QStoreOrderScheduleDay.storeOrderScheduleDay;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private final JPAQueryFactory queryFactory;

    // 조건에 맞는 가게 목록 조회
    public List<Store> searchStores(StoreSearchCondition condition) {
        JPAQuery<Store> query = queryFactory.selectFrom(store);

        // 공통 검색 조건 및 스케줄 조인 적용
        applySearchFilters(query, condition);

        return query.orderBy(getSortOrder(condition.sortType()))
                .distinct()
                .fetch();
    }

    // 페이징 처리를 위한 전체 개수 카운트 쿼리
    public long countStores(StoreSearchCondition condition) {
        JPAQuery<Long> query = queryFactory.select(store.countDistinct()).from(store);

        applySearchFilters(query, condition);

        Long count = query.fetchOne();
        return count != null ? count : 0L;
    }

    private void applySearchFilters(JPAQuery<?> query, StoreSearchCondition condition) {
        // 기본 가게 테이블 조건 적용
        query.where(
                keywordContains(condition.cleanKeyword()),
                districtEq(condition.district()),
                categoryEq(condition.category()),
                isBudgetInRange(condition.budget())
        );

        // 픽업 날짜, 시간, 수량 조건이 하나라도 있을 때만 스케줄 테이블 동적 조인
        if (hasScheduleCondition(condition)) {
            applyScheduleJoin(query, condition);
        }
    }

    private boolean hasScheduleCondition(StoreSearchCondition condition) {
        return condition.pickupDate() != null
                || (condition.pickupTimes() != null && !condition.pickupTimes().isEmpty())
                || condition.quantity() != null;
    }

    private void applyScheduleJoin(JPAQuery<?> query, StoreSearchCondition condition) {
        query.join(storeOrderSchedule)
                .on(cleanOnConditions(
                        storeOrderSchedule.store.id.eq(store.id),
                        storeOrderSchedule.deletedAt.isNull(),
                        isPickupDateInSchedulePeriod(condition.pickupDate())
                ))
                .join(storeOrderScheduleDay)
                .on(cleanOnConditions(
                        storeOrderScheduleDay.schedule.id.eq(storeOrderSchedule.id),
                        storeOrderScheduleDay.deletedAt.isNull(),
                        storeOrderScheduleDay.available.isTrue(),
                        isPickupDayAvailable(condition.pickupDate()),
                        isLeadTimeEnough(condition.pickupDate()),
                        isQuantitySatisfied(condition.quantity()),
                        isPickupTimesAvailable(condition.pickupTimes())
                ));
    }

    private BooleanExpression[] cleanOnConditions(BooleanExpression... conditions) {
        return java.util.Arrays.stream(conditions)
                .filter(java.util.Objects::nonNull)
                .toArray(BooleanExpression[]::new);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? store.storeName.contains(keyword) : null;
    }

    private BooleanExpression districtEq(String district) {
        return district != null ? store.district.eq(district) : null;
    }

    private BooleanExpression categoryEq(StoreCategory category) {
        return category != null ? store.category.eq(category) : null;
    }

    private BooleanExpression isBudgetInRange(Integer budget) {
        if (budget == null) return null;

        return store.minPrice.loe(budget) // 가게 최소 가격 <= 사용자 예산
                .and(store.maxPrice.goe(budget)); // 가게 최대 가격 >= 사용자 예산
    }

    private BooleanExpression isPickupDateInSchedulePeriod(LocalDate requestedDate) {
        if (requestedDate == null) return null;
        return storeOrderSchedule.startDate.loe(requestedDate)
                .and(storeOrderSchedule.endDate.goe(requestedDate));
    }

    private BooleanExpression isPickupDayAvailable(LocalDate requestedDate) {
        if (requestedDate == null) return null;
        return storeOrderScheduleDay.dayOfWeek.eq(requestedDate.getDayOfWeek());
    }

    private BooleanExpression isLeadTimeEnough(LocalDate requestedDate) {
        if (requestedDate == null) return null;
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), requestedDate);
        return storeOrderSchedule.minOrderDays.loe((int) daysBetween);
    }

    private BooleanExpression isQuantitySatisfied(Integer requestedQuantity) {
        if (requestedQuantity == null) return null;
        return storeOrderScheduleDay.minOrderQuantity.loe(requestedQuantity)
                .and(storeOrderScheduleDay.maxOrderQuantity.goe(requestedQuantity));
    }

    private BooleanExpression isPickupTimesAvailable(List<LocalTime> requestedTimes) {
        if (requestedTimes == null || requestedTimes.isEmpty()) return null;

        BooleanExpression result = null;
        for (LocalTime time : requestedTimes) {
            BooleanExpression timeCondition = storeOrderScheduleDay.pickupStartTime.loe(time)
                    .and(storeOrderScheduleDay.pickupEndTime.goe(time))
                    .and(isPickupTimeNotInBreakTime(time));

            result = (result == null) ? timeCondition : result.or(timeCondition);
        }
        return result;
    }

    private BooleanExpression isPickupTimeNotInBreakTime(LocalTime time) {
        return storeOrderScheduleDay.breakStartTime.isNull()
                .or(storeOrderScheduleDay.breakEndTime.isNull())
                .or(storeOrderScheduleDay.breakStartTime.gt(time))
                .or(storeOrderScheduleDay.breakEndTime.loe(time));
    }

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
            case ORDER_DESC -> store.id.desc();               // 주문 많은 순 (임시 최신순 대체)
        };
    }
}