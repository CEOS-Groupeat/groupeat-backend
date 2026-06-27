package com.groupeat.domain.orders.converter;

import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;

import java.util.List;
import java.util.Map;

public class OrderListConverter {

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

        // 무한 스크롤을 위한 nextCursor 계산
        Long nextCursor = cards.isEmpty() ? null : cards.get(cards.size() - 1).orderId();

        return OrderListResponse.builder()
                .totalElements(totalElements)
                .orderList(cards)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private static OrderListResponse.OrderCardDTO toOrderCardDTO(Order order, List<OrderItem> items) {
        String menuSummary = "메뉴 없음";
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
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .menuSummary(menuSummary)
                .orderStatus(order.getOrderStatus())
                .build();
    }
}
