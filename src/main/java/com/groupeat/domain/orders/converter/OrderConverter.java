package com.groupeat.domain.orders.converter;

import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.dto.response.OrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;

import java.time.LocalDate;
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

    public static OrderListResponse toOrderListResponse(
            List<Order> orders,
            long totalElements,
            boolean hasNext,
            Map<Long, List<OrderItem>> itemsByOrderId
    ) {
        List<OrderListResponse.OrderCardDTO> cards = orders.stream()
                .map(order -> {
                    List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    return toOrderCardDTO(order, items);
                })
                .toList();

        return OrderListResponse.builder()
                .orders(cards)
                .totalElements(totalElements)
                .hasNext(hasNext)
                .build();
    }

    private static OrderListResponse.OrderCardDTO toOrderCardDTO(Order order, List<OrderItem> items) {
        String menuSummary = "메뉴 정보 없음";
        if (!items.isEmpty()) {
            menuSummary = items.get(0).getMenuName();
            if (items.size() > 1) {
                menuSummary += " 외 " + (items.size() - 1) + "개";
            }
        }

        return OrderListResponse.OrderCardDTO.builder()
                .orderId(order.getId())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getStoreName())
                .storeImageUrl(order.getStore().getImageUrl())

                .orderDate(order.getCreatedAt().toLocalDate())
                .orderTime(order.getCreatedAt().toLocalTime())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())

                .menuSummary(menuSummary)
                .totalOriginalPrice(order.getTotalOriginalPrice())
                .paymentAmount(order.getPaymentAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }

    public static OrderDetailResponse toOrderDetailResponse(Order order, List<OrderItem> orderItems) {

        List<OrderDetailResponse.OrderDetailItemDTO> itemDTOs = orderItems.stream()
                .map(item -> OrderDetailResponse.OrderDetailItemDTO.builder()
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .items(itemDTOs)
                .paymentAmount(order.getPaymentAmount())
                .paymentMethod(order.getPaymentMethod())
                .orderDate(order.getCreatedAt().toLocalDate())
                .orderTime(order.getCreatedAt().toLocalTime())
                .build();
    }
}
