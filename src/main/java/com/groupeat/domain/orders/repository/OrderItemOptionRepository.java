package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {
}