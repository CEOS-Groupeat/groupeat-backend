package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.converter.OwnerOrderDetailConverter;
import com.groupeat.domain.orders.converter.OwnerOrderListConverter;
import com.groupeat.domain.orders.dto.response.OwnerOrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.OrderTab;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderItemOptionRepository;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderQueryRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.owner.validator.ActiveBusinessOwnerValidator;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.global.dto.CursorResponse;
import com.groupeat.global.exception.GeneralException;
import com.groupeat.global.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerOrderService {

    private final OrderQueryRepository orderQueryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final MenuRepository menuRepository;
    private final PaymentRepository paymentRepository;
    private final ActiveBusinessOwnerValidator activeBusinessOwnerValidator;

    public OwnerOrderListResponse.OwnerOrderListDTO getOwnerOrderListByTab(
            Long ownerId, OrderTab tab, LocalDate filterDate, Long lastOrderId, int size
    ) {
        activeBusinessOwnerValidator.validate(ownerId);

        List<OrderStatus> statuses = tab.getStatuses();
        LocalDate pickupDate = filterDate;

        // 확정 탭은 한 번에 300개, 일반 탭은 페이징을 위해 size + 1개 조회
        int fetchSize = tab.isConfirmedTab() ? 300 : size + 1;
        int targetSize = tab.isConfirmedTab() ? 300 : size; // CursorUtils에 넘길 기준 사이즈

        List<Order> orders = orderQueryRepository.findOwnerOrdersByCursorAndTab(
                ownerId, statuses, pickupDate, lastOrderId, fetchSize, tab
        );

        long totalElements = orderQueryRepository.countOwnerOrdersWithDate(
                ownerId, statuses, pickupDate
        );

        CursorResponse<Order> cursorResponse =
                CursorUtils.getCursorResponse(orders, targetSize, Order::getId);

        if (cursorResponse.content().isEmpty()) {
            return OwnerOrderListConverter.toEmptyResponse(totalElements);
        }

        List<Long> orderIds = cursorResponse.content().stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<Long> memberIds = cursorResponse.content().stream()
                .map(Order::getMemberId)
                .distinct()
                .toList();

        Set<Long> reorderMemberIds = orderQueryRepository.findReorderMemberIds(ownerId, memberIds, orderIds);

        return OwnerOrderListConverter.toOwnerOrderListDTO(
                cursorResponse, totalElements, itemsByOrderId, reorderMemberIds, tab
        );
    }

    public OwnerOrderDetailResponse.OrderDetailDTO getOwnerOrderDetail(Long ownerId, Long orderId) {
        // 사장님 계정 유효성 검증
        activeBusinessOwnerValidator.validate(ownerId);

        // 주문 데이터 조회
        Order order = orderRepository.findByIdAndOwnerIdWithItems(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findFirstByOrderId(order.getOrderId()).orElse(null);

        List<Long> orderItemIds = order.getOrderItems().stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList());

        // IN 쿼리로 연관된 옵션들을 한 방에 조회 후 Map으로 그룹화
        List<OrderItemOption> allOptions = orderItemOptionRepository.findByOrderItemIdIn(orderItemIds);
        Map<Long, List<OrderItemOption>> optionsByOrderItemId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getOrderItem().getId()));

        List<Long> menuIds = order.getOrderItems().stream()
                .map(OrderItem::getMenuId)
                .distinct()
                .toList();

        Map<Long, String> menuImageUrls = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(
                        Menu::getId,
                        Menu::getImageUrl,
                        (existing, replacement) -> existing
                ));

        return OwnerOrderDetailConverter.toOrderDetailDTO(order, payment, optionsByOrderItemId, menuImageUrls);
    }
}
