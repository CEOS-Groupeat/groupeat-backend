package com.groupeat.domain.orders.converter;

import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OwnerOrderListConverter {

    public static OwnerOrderListResponse.OwnerOrderListDTO toOwnerOrderListDTO(
            List<Order> orders,
            long totalElements,
            boolean hasNext,
            Long nextCursor,
            Map<Long, List<OrderItem>> itemsByOrderId,
            Set<Long> reorderMemberIds,
            boolean isConfirmedTab
    ) {
        List<OwnerOrderListResponse.OrderCardDTO> cardDTOs = orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    boolean isReorder = reorderMemberIds.contains(order.getMemberId());

                    return toOrderCardDTO(order, orderItems, isReorder, isConfirmedTab);
                })
                .collect(Collectors.toList());

        return OwnerOrderListResponse.OwnerOrderListDTO.builder()
                .totalElements(totalElements)
                .orderList(cardDTOs)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private static OwnerOrderListResponse.OrderCardDTO toOrderCardDTO(
            Order order,
            List<OrderItem> orderItems,
            boolean isReorder,
            boolean isConfirmedTab
    ) {
        List<OwnerOrderListResponse.OrderCardItemDTO> itemDTOs = orderItems.stream()
                .map(item -> OwnerOrderListResponse.OrderCardItemDTO.builder()
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OwnerOrderListResponse.OrderCardDTO.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .customerName(order.getCustomerName())
                .groupName(order.getGroupName())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .totalAmount(order.getPaymentAmount())
                .items(itemDTOs)
                .isReorder(isReorder)
                .paymentMethod(isConfirmedTab ? order.getPaymentMethod() : null)
                .build();
    }
}