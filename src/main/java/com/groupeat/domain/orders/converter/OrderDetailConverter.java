package com.groupeat.domain.orders.converter;

import com.groupeat.domain.orders.dto.response.OrderDetailResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.payment.entity.Payment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderDetailConverter {

    public static OrderDetailResponse.OrderDetailDTO toOrderDetailDTO(
            Order order,
            Payment payment,
            Map<Long, List<OrderItemOption>> optionsByOrderItemId
    ) {
        // 주문자 정보 매핑
        OrderDetailResponse.OrdererInfoDTO ordererInfo = OrderDetailResponse.OrdererInfoDTO.builder()
                .customerName(order.getCustomerName())
                .groupName(order.getGroupName())
                .phoneNumber(order.getCustomerPhone())
                .orderDate(order.getCreatedAt().toLocalDate())
                .orderTime(order.getCreatedAt().toLocalTime())
                .requests(order.getRequests())
                .build();

        // 주문 상품 정보 매핑
        List<OrderDetailResponse.OrderMenuDTO> orderMenus = order.getOrderItems().stream()
                .map(item -> {
                    List<OrderItemOption> options = optionsByOrderItemId.getOrDefault(item.getId(), List.of());

                    List<OrderDetailResponse.OrderMenuOptionDTO> optionDTOs = options.stream()
                            .map(opt -> OrderDetailResponse.OrderMenuOptionDTO.builder()
                                    .optionName(opt.getOptionName())
                                    .build())
                            .collect(Collectors.toList());

                    int itemOriginalPrice = item.getUnitPrice() * item.getQuantity();
                    int itemDiscountRate = itemOriginalPrice > 0 ?
                            (int) Math.round((double) item.getDiscountAmount() / itemOriginalPrice * 100) : 0;

                    return OrderDetailResponse.OrderMenuDTO.builder()
                            .menuName(item.getMenuName())
                            .options(optionDTOs)
                            .quantity(item.getQuantity())
                            .menuImageUrl(null) // 필요시 매핑
                            .discountRate(itemDiscountRate)
                            .totalAmount(item.getFinalPrice())
                            .build();
                })
                .collect(Collectors.toList());

        // 결제 정보 매핑
        int totalQuantity = order.getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum();
        int totalOriginalPrice = order.getTotalOriginalPrice() != null ? order.getTotalOriginalPrice() : 0;
        int perPersonAmount = (totalQuantity > 0) ? (totalOriginalPrice / totalQuantity) : 0;

        int totalDiscountAmount = order.getTotalDiscountAmount() != null ? order.getTotalDiscountAmount() : 0;
        int totalDiscountRate = totalOriginalPrice > 0 ?
                (int) Math.round((double) totalDiscountAmount / totalOriginalPrice * 100) : 0;

        OrderDetailResponse.PaymentInfoDTO paymentInfo = OrderDetailResponse.PaymentInfoDTO.builder()
                .paymentMethod(order.getPaymentMethod())
                .paymentMeans(payment != null ? payment.getPaymentProvider() : null)
                .perPersonAmount(perPersonAmount)
                .discountRate(totalDiscountRate)
                .totalDiscountAmount(totalDiscountAmount)
                .originalTotalAmount(totalOriginalPrice)
                .finalPaymentAmount(order.getPaymentAmount())
                .build();

        // 최종 조립
        return OrderDetailResponse.OrderDetailDTO.builder()
                .storeName(order.getStore().getStoreName())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .ordererInfo(ordererInfo)
                .orderMenus(orderMenus)
                .paymentInfo(paymentInfo)
                .orderStatus(order.getOrderStatus())
                .build();
    }
}
