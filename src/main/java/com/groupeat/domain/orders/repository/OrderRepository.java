package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    Optional<Order> findByIdAndStoreOwnerId(Long orderId, Long ownerId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId AND o.store.ownerId = :ownerId")
    Optional<Order> findByIdAndStoreOwnerIdWithItems(
            @Param("orderId") Long orderId,
            @Param("ownerId") Long ownerId
    );

    // OrderItem 리스트를 한 번에 패치 조인
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId AND o.memberId = :memberId")
    Optional<Order> findByIdAndMemberIdWithItems(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId AND o.store.ownerId = :ownerId")
    Optional<Order> findByIdAndOwnerIdWithItems(
            @Param("orderId") Long orderId,
            @Param("ownerId") Long ownerId
    );

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM Order o
            JOIN o.orderItems oi
            WHERE o.store.id = :storeId
              AND o.pickupDate = :pickupDate
              AND o.orderStatus = :orderStatus
            """)
    Long sumOrderItemQuantityByStoreIdAndPickupDateAndStatus(
            @Param("storeId") Long storeId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("orderStatus") OrderStatus orderStatus
    );
}
