package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    // OrderItem 리스트를 한 번에 패치 조인
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId AND o.memberId = :memberId")
    Optional<Order> findByIdAndMemberIdWithItems(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );
}
