package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.OrderTab;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.groupeat.domain.orders.entity.QOrder.order;
import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 커서 기반 조건 검색(고객용)
    public List<Order> findOrdersByCursor(Long memberId, List<OrderStatus> statuses, Long lastOrderId, int size) {
        return queryFactory
                .selectFrom(order)
                .join(order.store, store).fetchJoin()
                .where(
                        order.memberId.eq(memberId),
                        statusIn(statuses),
                        ltOrderId(lastOrderId)
                )
                .orderBy(order.id.desc())
                .limit(size + 1)
                .fetch();
    }

    // 카운트 쿼리 (전체 개수 조회)
    public long countOrders(Long memberId, List<OrderStatus> statuses) {
        Long count = queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.memberId.eq(memberId),
                        statusIn(statuses)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    // 사장님용
    public List<Order> findOwnerOrdersByCursorAndTab(
            Long ownerId, List<OrderStatus> statuses, LocalDate pickupDate,
            Long lastOrderId, int size, OrderTab tab
    ) {
        return queryFactory
                .selectFrom(order)
                .join(order.store, store).fetchJoin()
                .where(
                        store.ownerId.eq(ownerId),
                        statusIn(statuses),
                        pickupDateEq(pickupDate),
                        dynamicCursor(tab, lastOrderId)
                )
                .orderBy(dynamicOrderSpecifier(tab))
                .limit(size + 1)
                .fetch();
    }

    private OrderSpecifier<?>[] dynamicOrderSpecifier(OrderTab tab) {
        if (tab.isConfirmedTab()) {
            // 확정 탭: 픽업 날짜 오름차순 -> 픽업 시간 오름차순 -> 겹치면 ID 오름차순
            return new OrderSpecifier[]{
                    order.pickupDate.asc(),
                    order.pickupTime.asc(),
                    order.id.asc()
            };
        } else {
            // 대기 중, 지난 주문 탭: 최신순
            return new OrderSpecifier[]{ order.id.desc() };
        }
    }

    private BooleanExpression dynamicCursor(OrderTab tab, Long lastOrderId) {
        if (lastOrderId == null) return null;

        if (tab.isConfirmedTab()) {
            return null;
        } else {
            return order.id.lt(lastOrderId);
        }
    }

    public long countOwnerOrdersWithDate(Long ownerId, List<OrderStatus> statuses, LocalDate pickupDate) {
        Long count = queryFactory
                .select(order.count())
                .from(order)
                .join(order.store, store)
                .where(
                        store.ownerId.eq(ownerId),
                        statusIn(statuses),
                        pickupDateEq(pickupDate)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    public Set<Long> findReorderMemberIds(Long ownerId, List<Long> memberIds, List<Long> currentOrderIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }

        List<Long> reorderIds = queryFactory
                .select(order.memberId).distinct()
                .from(order)
                .join(order.store, store)
                .where(
                        store.ownerId.eq(ownerId),
                        order.memberId.in(memberIds),
                        order.orderStatus.eq(OrderStatus.COMPLETED),
                        orderIdNotIn(currentOrderIds)
                )
                .fetch();

        return new java.util.HashSet<>(reorderIds);
    }

    // 커서 조건: 최초 조회 시(lastOrderId == null) 조건 패스, 두 번째부터 조건 적용
    private BooleanExpression ltOrderId(Long lastOrderId) {
        return lastOrderId != null ? order.id.lt(lastOrderId) : null;
    }

    private BooleanExpression statusIn(List<OrderStatus> statuses) {
        return (statuses != null && !statuses.isEmpty()) ? order.orderStatus.in(statuses) : null;
    }

    private BooleanExpression pickupDateEq(LocalDate pickupDate) {
        return pickupDate != null ? order.pickupDate.eq(pickupDate) : null;
    }

    // currentOrderIds가 비어있지 않을 때만 notIn 조건 적용
    private BooleanExpression orderIdNotIn(List<Long> currentOrderIds) {
        return (currentOrderIds != null && !currentOrderIds.isEmpty()) ?
                order.id.notIn(currentOrderIds) : null;
    }
}