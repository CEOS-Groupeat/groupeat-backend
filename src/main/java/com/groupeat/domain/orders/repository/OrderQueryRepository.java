package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groupeat.domain.orders.entity.QOrder.order;
import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 커서 기반 조건 검색
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

    // 커서 조건: 최초 조회 시(lastOrderId == null) 조건 패스, 두 번째부터 조건 적용
    private BooleanExpression ltOrderId(Long lastOrderId) {
        return lastOrderId != null ? order.id.lt(lastOrderId) : null;
    }

    private BooleanExpression statusIn(List<OrderStatus> statuses) {
        return (statuses != null && !statuses.isEmpty()) ? order.orderStatus.in(statuses) : null;
    }
}