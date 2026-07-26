package com.groupeat.domain.orders.converter;

import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.global.dto.CursorResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderListConverter {

    public static OrderListResponse toOrderListResponse(
            com.groupeat.global.dto.CursorResponse<Order> cursorResponse,
            long totalElements,
            Map<Long, List<OrderItem>> itemsByOrderId,
            Set<Long> reviewedOrderIds
    ) {
        CursorResponse<OrderListResponse.OrderCardDTO> dtoCursorResponse =
                cursorResponse.map(order -> {
                    List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    return toOrderCardDTO(order, items, reviewedOrderIds);
                });

        return OrderListResponse.builder()
                .totalElements(totalElements)
                .orderList(dtoCursorResponse.content())
                .hasNext(dtoCursorResponse.hasNext())
                .nextCursor(dtoCursorResponse.nextCursor())
                .build();
    }

    private static OrderListResponse.OrderCardDTO toOrderCardDTO(Order order, List<OrderItem> items, Set<Long> reviewedOrderIds) {
        String menuSummary = "메뉴 없음";
        if (!items.isEmpty()) {
            menuSummary = items.get(0).getMenuName();
            if (items.size() > 1) {
                menuSummary += " 외 " + (items.size() - 1) + "개";
            }
        }

        boolean isPickupComplete = order.getOrderStatus() == OrderStatus.COMPLETED;
        boolean hasReview = isPickupComplete && reviewedOrderIds.contains(order.getId());

        return OrderListResponse.OrderCardDTO.builder()
                .orderId(order.getId())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getStoreName())
                .storeImageUrl(order.getStore().getImageUrl())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .menuSummary(menuSummary)
                .orderStatus(order.getOrderStatus())
                .hasReview(hasReview)
                .build();
    }
}
