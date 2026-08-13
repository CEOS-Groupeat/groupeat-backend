package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {
    List<OrderItemOption> findByOrderItemIdIn(List<Long> orderItemIds);
}