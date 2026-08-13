package com.groupeat.domain.cart.repository;

import com.groupeat.domain.cart.entity.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);

    // 동시성 방지를 위한 쓰기 락(Pessimistic Write) 적용 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.memberId = :memberId")
    Optional<Cart> findByMemberIdWithPessimisticLock(@Param("memberId") Long memberId);
}
