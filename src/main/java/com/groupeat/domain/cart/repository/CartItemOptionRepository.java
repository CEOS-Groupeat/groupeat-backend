package com.groupeat.domain.cart.repository;

import com.groupeat.domain.cart.entity.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Long> {
    // 여러 장바구니 항목들의 옵션을 한 번에 가져오기 위한 메서드
    List<CartItemOption> findAllByCartItemIdIn(List<Long> cartItemIds);

    // 장바구니 항목 ID로 엮인 옵션들을 한 번에 지우는 메서드
    void deleteAllByCartItemId(Long cartItemId);

    // 특정 장바구니 아이템의 옵션들 찾기
    List<CartItemOption> findByCartItemId(Long cartItemId);

    @Modifying
    @Query("""
            DELETE FROM CartItemOption option
            WHERE option.cartItem.id IN (
                SELECT item.id FROM CartItem item WHERE item.cart.memberId = :memberId
            )
            """)
    void deleteAllByCartMemberId(@Param("memberId") Long memberId);
}
