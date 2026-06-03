package com.groupeat.domain.settlement.repository;

import com.groupeat.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<Settlement> findByOrderId(Long orderId);
}
