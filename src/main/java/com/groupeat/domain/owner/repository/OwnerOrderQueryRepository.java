package com.groupeat.domain.owner.repository;

import com.groupeat.domain.orders.enums.OrderStatus;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.groupeat.domain.orders.entity.QOrder.order;
import static com.groupeat.domain.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class OwnerOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Map<OrderStatus, Long> countOrdersByStatuses(Long ownerId, List<OrderStatus> statuses) {
        List<Tuple> results = queryFactory
                .select(order.orderStatus, order.count())
                .from(order)
                .join(order.store, store)
                .where(
                        store.ownerId.eq(ownerId),
                        order.orderStatus.in(statuses)
                )
                .groupBy(order.orderStatus)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(order.orderStatus),
                        tuple -> tuple.get(order.count())
                ));
    }
}
