package com.groupeat.domain.orders.repository;

import com.groupeat.domain.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
