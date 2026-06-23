package com.groupeat.domain.orders.converter;

import com.groupeat.domain.orders.dto.response.OwnerOrderDetailResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.payment.entity.Payment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OwnerOrderDetailConverter {

    public static OwnerOrderDetailResponse.OrderDetailDTO toOrderDetailDTO(Order order, Payment payment, Map<Long, List<OrderItemOption>> optionsByOrderItemId) {

        // 주문자 정보 매핑
        OwnerOrderDetailResponse.OrdererInfoDTO ordererInfo = OwnerOrderDetailResponse.OrdererInfoDTO.builder()
                .customerName(order.getCustomerName())
                .groupName(order.getGroupName())
                .phoneNumber(order.getCustomerPhone())
                .orderDate(order.getCreatedAt().toLocalDate())
                .requests(order.getRequests())
                .build();

        // 주문 상품 정보 매핑
        List<OwnerOrderDetailResponse.OrderMenuDTO> orderMenus = order.getOrderItems().stream()
                .map(item -> {
                    List<OrderItemOption> options = optionsByOrderItemId.getOrDefault(item.getId(), List.of());

                    List<OwnerOrderDetailResponse.OrderMenuOptionDTO> optionDTOs = item.getOrderItemOptions().stream()
                            .map(opt -> OwnerOrderDetailResponse.OrderMenuOptionDTO.builder()
                                    .optionName(opt.getOptionName())
                                    .build())
                            .collect(Collectors.toList());

                    int itemOriginalPrice = item.getUnitPrice() * item.getQuantity();
                    int itemDiscountRate = itemOriginalPrice > 0 ?
                            (int) Math.round((double) item.getDiscountAmount() / itemOriginalPrice * 100) : 0;

                    return OwnerOrderDetailResponse.OrderMenuDTO.builder()
                            .menuName(item.getMenuName())
                            .options(optionDTOs)
                            .quantity(item.getQuantity())
                            .menuImageUrl(null)
                            .discountRate(itemDiscountRate)
                            .totalAmount(item.getFinalPrice())
                            .build();
                })
                .collect(Collectors.toList());

        // 결제 정보 매핑 및 1인당 금액 계산
        int totalQuantity = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        // 1인당 금액 = 총 원가 / 주문 수량
        int totalOriginalPrice = order.getTotalOriginalPrice() != null ? order.getTotalOriginalPrice() : 0;
        int perPersonAmount = (totalQuantity > 0) ? (totalOriginalPrice / totalQuantity) : 0;

        // 전체 할인율 계산
        int totalDiscountAmount = order.getTotalDiscountAmount() != null ? order.getTotalDiscountAmount() : 0;
        int totalDiscountRate = totalOriginalPrice > 0 ?
                (int) Math.round((double) totalDiscountAmount / totalOriginalPrice * 100) : 0;

        OwnerOrderDetailResponse.PaymentInfoDTO paymentInfo = OwnerOrderDetailResponse.PaymentInfoDTO.builder()
                .paymentMethod(order.getPaymentMethod())
                .paymentMeans(payment != null ? payment.getPaymentProvider() : null)
                .perPersonAmount(perPersonAmount)
                .discountRate(totalDiscountRate)
                .totalDiscountAmount(totalDiscountAmount)
                .originalTotalAmount(totalOriginalPrice)
                .finalPaymentAmount(order.getPaymentAmount())
                .build();

        return OwnerOrderDetailResponse.OrderDetailDTO.builder()
                .ordererInfo(ordererInfo)
                .orderMenus(orderMenus)
                .paymentInfo(paymentInfo)
                .build();
    }
}