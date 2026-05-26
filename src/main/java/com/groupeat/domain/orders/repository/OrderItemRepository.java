package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 연관된 주문 상품들 가져오기
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}
