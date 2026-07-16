package com.groupeat.domain.payment.repository;

import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.payment.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findReadOnlyByOrderId(String orderId);

    Optional<Payment> findFirstByOrderId(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    @Query("""
            SELECT DISTINCT p
            FROM Payment p
            JOIN FETCH p.order o
            JOIN FETCH o.store
            LEFT JOIN FETCH o.orderItems
            WHERE p.paymentStatus = :paymentStatus
              AND o.orderStatus = :orderStatus
              AND p.approvedAt > :approvedAfter
              AND p.approvedAt <= :approvedAtOrBefore
            """)
    List<Payment> findAllOrderAcceptDeadlineCandidates(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("approvedAfter") LocalDateTime approvedAfter,
            @Param("approvedAtOrBefore") LocalDateTime approvedAtOrBefore
    );

    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.order o
            WHERE p.paymentStatus = :paymentStatus
              AND o.orderStatus = :orderStatus
              AND p.approvedAt <= :approvedAtOrBefore
            ORDER BY p.approvedAt ASC, p.id ASC
            """)
    List<Payment> findAllAutoRejectCandidates(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("approvedAtOrBefore") LocalDateTime approvedAtOrBefore,
            Pageable pageable
    );
}
