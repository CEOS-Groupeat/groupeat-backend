package com.groupeat.domain.orders.service;

import com.groupeat.domain.cart.dto.request.CartCalculateRequest;
import com.groupeat.domain.cart.dto.response.CartCalculateResponse;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.cart.exception.CartErrorStatus;
import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
import com.groupeat.domain.cart.service.CartCalculateService;
import com.groupeat.domain.orders.converter.OrderConverter;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.dto.response.OrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderItemOptionRepository;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderQueryRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentType;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.MenuOptionRepository;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final PaymentRepository paymentRepository;

    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    private final CartCalculateService cartCalculateService;

    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        List<Long> cartItemIds = request.cartItemIds();

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
        if (cartItems.size() != cartItemIds.size()) {
            throw new GeneralException(CartErrorStatus.CART_ITEM_NOT_FOUND);
        }

        // 2. 픽업 날짜/시간 동일성 검증 (하나라도 다르면 주문 불가)
        long distinctPickupCount = cartItems.stream()
                .map(item -> item.getPickupDate().toString() + item.getPickupTime().toString())
                .distinct()
                .count();
        if (distinctPickupCount > 1) {
            throw new GeneralException(CartErrorStatus.DIFFERENT_PICKUP_TIME);
        }

        LocalDate pickupDate = cartItems.get(0).getPickupDate();
        LocalTime pickupTime = cartItems.get(0).getPickupTime();

        Long storeId = cartItems.get(0).getStoreId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        Map<Long, Menu> menuMap = menuRepository.findAllById(cartItems.stream().map(CartItem::getMenuId).toList())
                .stream().collect(Collectors.toMap(Menu::getId, m -> m));

        List<CartItemOption> allCartItemOptions = cartItemOptionRepository.findAllByCartItemIdIn(cartItemIds);
        Map<Long, List<CartItemOption>> optionsMap = allCartItemOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getCartItem().getId()));

        Map<Long, MenuOption> realOptionsMap = menuOptionRepository.findAllById(allCartItemOptions.stream().map(CartItemOption::getMenuOptionId).toList())
                .stream().collect(Collectors.toMap(MenuOption::getId, o -> o));

        CartCalculateResponse calculated = cartCalculateService.calculateWithEntities(
                cartItems, store, menuMap, optionsMap, realOptionsMap
        );

        // 결제 금액 계산 및 주문 번호 발급
        int finalPaymentAmount = (int) (calculated.finalPaymentAmount() * request.paymentMethod().getPaymentRatio());
        String generatedOrderId = "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        // Order 엔티티 생성 및 저장
        Order order = OrderConverter.toOrder(
                generatedOrderId, memberId, store,
                calculated.totalOriginalPrice(), calculated.totalDiscountAmount(),
                finalPaymentAmount, pickupDate, pickupTime, request
        );
        Order savedOrder = orderRepository.save(order);
        Payment payment = Payment.ready(
                savedOrder,
                memberId,
                PaymentType.valueOf(request.paymentMethod().name()),
                calculated.finalPaymentAmount(),
                finalPaymentAmount
        );
        Payment savedPayment = paymentRepository.save(payment);

        // OrderItem 및 Option 매핑 및 저장
        Map<Long, CartCalculateResponse.CalculatedItem> calcMap = calculated.calculatedItems().stream()
                .collect(Collectors.toMap(CartCalculateResponse.CalculatedItem::cartItemId, item -> item));
        List<OrderItemOption> orderItemOptionsToSave = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Menu menu = menuMap.get(cartItem.getMenuId());
            CartCalculateResponse.CalculatedItem calcItem = calcMap.get(cartItem.getId());

            OrderItem orderItem = OrderConverter.toOrderItem(
                    savedOrder, cartItem, menu, calcItem.unitPrice(),
                    calcItem.itemDiscountAmount(), calcItem.itemFinalPrice()
            );
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            List<CartItemOption> cartItemOptions = optionsMap.getOrDefault(cartItem.getId(), List.of());
            for (CartItemOption cartItemOption : cartItemOptions) {
                MenuOption realOption = realOptionsMap.get(cartItemOption.getMenuOptionId());
                OrderItemOption orderItemOption = OrderConverter.toOrderItemOption(savedOrderItem, realOption);
                orderItemOptionsToSave.add(orderItemOption);
            }
        }

        if (!orderItemOptionsToSave.isEmpty()) {
            orderItemOptionRepository.saveAll(orderItemOptionsToSave);
        }

        // 장바구니 비우기
        if (!allCartItemOptions.isEmpty()) {
            cartItemOptionRepository.deleteAllInBatch(allCartItemOptions);
        }
        cartItemRepository.deleteAllByIdInBatch(cartItemIds);

        return OrderConverter.toOrderCreateResponse(savedOrder, savedPayment.getId());
    }

    @Transactional(readOnly = true)
    public OrderListResponse getOrderList(Long memberId, List<OrderStatus> statuses, Long lastOrderId, int size) {
        // 전체 카운트 조회
        long totalElements = orderQueryRepository.countOrders(memberId, statuses);

        // 커서 기반 주문 목록 조회 (요청 size + 1개 가져옴)
        List<Order> orders = orderQueryRepository.findOrdersByCursor(memberId, statuses, lastOrderId, size);

        // 다음 페이지 여부 확인 및 데이터 슬라이싱
        boolean hasNext = false;
        if (orders.size() > size) {
            hasNext = true;
            orders = orders.subList(0, size); // +1 확인용으로 가져온 마지막 데이터 제외
        }

        if (orders.isEmpty()) {
            return OrderListResponse.builder().orders(List.of()).totalElements(totalElements).hasNext(false).build();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        return OrderConverter.toOrderListResponse(orders, totalElements, hasNext, itemsByOrderId);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {

        Order order = orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        return OrderConverter.toOrderDetailResponse(order, order.getOrderItems());
    }
}
