package com.groupeat.domain.orders.converter;

import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;

import java.time.LocalDateTime;

public class OrderConverter {

    public static OrderItem toOrderItem(Order order, CartItem cartItem, Menu menu, int unitPrice, int itemDiscountAmount, int itemFinalPrice) {
        return OrderItem.builder()
                .order(order)
                .menuId(menu.getId())
                .menuName(menu.getName())
                .quantity(cartItem.getQuantity())
                .unitPrice(unitPrice)
                .discountAmount(itemDiscountAmount)
                .finalPrice(itemFinalPrice)
                .build();
    }

    public static OrderItemOption toOrderItemOption(OrderItem orderItem, MenuOption menuOption) {
        return OrderItemOption.builder()
                .orderItem(orderItem)
                .optionId(menuOption.getId())
                .optionName(menuOption.getName())
                .optionPrice(menuOption.getAdditionalPrice())
                .build();
    }

    // Order 마스터 엔티티 생성
    public static Order toOrder(
            String orderId, Long memberId, Store store,
            int totalOriginalPrice, int totalDiscountAmount, int paymentAmount,
            LocalDateTime pickupDateTime, OrderCreateRequest request
    ) {
        return Order.builder()
                .orderId(orderId)
                .memberId(memberId)
                .store(store)
                .totalOriginalPrice(totalOriginalPrice)
                .totalDiscountAmount(totalDiscountAmount)
                .paymentAmount(paymentAmount)
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .groupName(request.groupName())
                .requests(request.requests())
                .pickupDateTime(pickupDateTime)
                .paymentMethod(request.paymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .build();
    }

    // 응답 DTO 생성
    public static OrderCreateResponse toOrderCreateResponse(Order order) {
        return OrderCreateResponse.builder()
                .paymentId(order.getId())
                .orderId(order.getOrderId())
                .amount(order.getPaymentAmount())
                .customerName(order.getCustomerName())
                .build();
    }
}