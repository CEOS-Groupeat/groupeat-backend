package com.groupeat.domain.orders.converter;

import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderTab;

import java.time.Duration;
import java.time.LocalDateTime;
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
            OrderTab tab
    ) {
        List<OwnerOrderListResponse.OrderCardDTO> cardDTOs = orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    boolean isReorder = reorderMemberIds.contains(order.getMemberId());

                    return toOrderCardDTO(order, orderItems, isReorder, tab);
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
            OrderTab tab
    ) {
        List<OwnerOrderListResponse.OrderCardItemDTO> itemDTOs = orderItems.stream()
                .map(item -> OwnerOrderListResponse.OrderCardItemDTO.builder()
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // 남은 시간 계산 로직 (WAITING 탭일 때만 계산)
        Integer remainingHours = null;
        if (tab == OrderTab.WAITING) {
            LocalDateTime deadline = order.getCreatedAt().plusHours(24);
            long hours = Duration.between(LocalDateTime.now(), deadline).toHours();
            // 시간이 이미 초과되었을 경우 음수가 나오는 것을 방지 (0으로 처리)
            remainingHours = (int) Math.max(0, hours);
        }

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
                .paymentMethod(tab.isConfirmedTab() ? order.getPaymentMethod() : null)
                .canCompletePickup(order.canCompletePickupAt(LocalDateTime.now()))
                .remainingHours(remainingHours)
                .build();
    }
}
