package com.groupeat.domain.cart.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "cart_item")
public class CartItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "pickup_datetime", nullable = false)
    private LocalDateTime pickupDateTime;

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}