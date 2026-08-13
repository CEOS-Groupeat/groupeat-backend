package com.groupeat.domain.cart.repository;

import com.groupeat.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByCartId(Long cartId);

    // 장바구니 ID, 메뉴 ID, 픽업 날짜, 픽업 시간이 일치하는 아이템들 찾기
    List<CartItem> findByCartIdAndMenuIdAndPickupDateAndPickupTime(
            Long cartId, Long menuId, LocalDate pickupDate, LocalTime pickupTime);

    @Modifying
    @Query("DELETE FROM CartItem item WHERE item.cart.memberId = :memberId")
    void deleteAllByCartMemberId(@Param("memberId") Long memberId);
}
