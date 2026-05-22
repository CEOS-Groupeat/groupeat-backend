package com.groupeat.domain.search.repository;

import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.entity.StoreSortType;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreCategory;
import com.groupeat.domain.store.entity.StoreRegion;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private final JPAQueryFactory queryFactory;

    // 조건에 맞는 가게 목록 조회
    public List<Store> searchStores(StoreSearchCondition condition) {
        return queryFactory
                .selectFrom(store)
                .where(
                        keywordContains(condition.keyword()),
                        regionEq(condition.region()),
                        categoryEq(condition.category()),
                        budgetLessThanOrEqualTo(condition.budget()),
                        isPickupTimeAvailable(condition.pickupTime()),
                        isPickupDateAvailable(condition.pickupDate()),
                        isQuantitySatisfied(condition.quantity())
                )
                .orderBy(getSortOrder(condition.sortType())) // 정렬 동적 제어
                .fetch();
    }

    // 페이징 처리를 위한 전체 개수 카운트 쿼리
    public long countStores(StoreSearchCondition condition) {
        Long count = queryFactory
                .select(store.count())
                .from(store)
                .where(
                        keywordContains(condition.keyword()),
                        regionEq(condition.region()),
                        categoryEq(condition.category()),
                        budgetLessThanOrEqualTo(condition.budget()),
                        isPickupTimeAvailable(condition.pickupTime()),
                        isPickupDateAvailable(condition.pickupDate()),
                        isQuantitySatisfied(condition.quantity())
                )
                .fetchOne();
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

    private BooleanExpression isPickupTimeAvailable(LocalTime requestedTime) {
        if (requestedTime == null) return null;

        return store.pickupOpenTime.loe(requestedTime)
                .and(store.pickupCloseTime.goe(requestedTime));
    }

    private BooleanExpression isPickupDateAvailable(LocalDate requestedDate) {
        if (requestedDate == null) return null;

        // 휴무일 검증: 가게의 closedDays 문자열에 요청한 날짜의 요일(예: MONDAY)이 포함되어 있지 않아야 함
        String dayOfWeek = requestedDate.getDayOfWeek().name();
        BooleanExpression isNotClosed = store.closedDays.contains(dayOfWeek).not();

        // 리드타임 검증: 오늘부터 요청 날짜까지의 차이가 가게의 최소 주문 일수(minOrderDays) 이상이어야 함
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), requestedDate);
        BooleanExpression isLeadTimeEnough = store.minOrderDays.loe((int) daysBetween);

        return isNotClosed.and(isLeadTimeEnough);
    }

    private BooleanExpression isQuantitySatisfied(Integer requestedQuantity) {
        if (requestedQuantity == null) return null;

        // 사용자가 입력한 수량이 가게의 할인 조건 수량보다 크거나 같아야 함 (조건 충족 가게만 노출)
        return store.discountConditionQuantity.loe(requestedQuantity);
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