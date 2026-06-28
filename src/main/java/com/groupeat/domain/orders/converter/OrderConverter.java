package com.groupeat.domain.orders.converter;

import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.dto.response.OrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.dto.response.OrderStatusChangeResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
            LocalDate pickupDate, LocalTime pickupTime, OrderCreateRequest request
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
                .pickupDate(pickupDate)
                .pickupTime(pickupTime)
                .paymentMethod(request.paymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .build();
    }

    // 응답 DTO 생성
    public static OrderCreateResponse toOrderCreateResponse(Order order, Long paymentId) {
        return OrderCreateResponse.builder()
                .paymentId(paymentId)
                .orderId(order.getOrderId())
                .amount(order.getPaymentAmount())
                .customerName(order.getCustomerName())
                .build();
    }



    public static OrderStatusChangeResponse toOrderStatusChangeResponse(Order order, LocalDateTime processedAt) {
        return new OrderStatusChangeResponse(
                order.getId(),
                order.getOrderStatus(),
                processedAt.toLocalDate(),
                processedAt.toLocalTime()
        );
    }
}
