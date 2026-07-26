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
import com.groupeat.domain.orders.converter.OrderDetailConverter;
import com.groupeat.domain.orders.converter.OrderListConverter;
import com.groupeat.domain.orders.dto.OrderCancelPreparation;
import com.groupeat.domain.orders.dto.OrderRejectPreparation;
import com.groupeat.domain.orders.dto.request.OrderCancelRequest;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCancelResponse;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.dto.response.OrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OrderListResponse;
import com.groupeat.domain.orders.dto.response.OrderStatusChangeResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderItemOptionRepository;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderQueryRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.dto.PaymentCancelResult;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.enums.PaymentType;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.payment.service.PaymentCancelService;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.review.repository.ReviewRepository;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.MenuOptionRepository;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.dto.CursorResponse;
import com.groupeat.global.exception.GeneralException;
import com.groupeat.global.util.CursorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private static final String OWNER_REJECT_CANCEL_REASON = "사업자 주문 거절";

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

    private final ReviewRepository reviewRepository;

    private final CartCalculateService cartCalculateService;
    private final PaymentCancelService paymentCancelService;
    private final OrderCancelTransactionService orderCancelTransactionService;
    private final OrderOwnerActionTransactionService orderOwnerActionTransactionService;
    private final OrderScheduleValidationService orderScheduleValidationService;

    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        List<Long> cartItemIds = request.cartItemIds();

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
        if (cartItems.size() != cartItemIds.size()) {
            throw new GeneralException(CartErrorStatus.CART_ITEM_NOT_FOUND);
        }

        // 픽업 날짜/시간 동일성 검증 (하나라도 다르면 주문 불가)
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
        int totalQuantity = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        orderScheduleValidationService.validateOrderCreation(storeId, pickupDate, pickupTime, totalQuantity);

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

        return OrderConverter.toOrderCreateResponse(savedOrder, savedPayment.getId());
    }

    @Transactional(readOnly = true)
    public OrderListResponse getOrderList(Long memberId, List<OrderStatus> statuses, Long lastOrderId, int size) {
        // 전체 카운트 조회
        long totalElements = orderQueryRepository.countOrders(memberId, statuses);

        // size + 1개 조회
        List<Order> orders = orderQueryRepository.findOrdersByCursor(memberId, statuses, lastOrderId, size + 1);

        CursorResponse<Order> cursorResponse =
                CursorUtils.getCursorResponse(orders, size, Order::getId);

        if (cursorResponse.content().isEmpty()) {
            return OrderListResponse.builder().orderList(List.of()).totalElements(totalElements).hasNext(false).build();
        }

        List<Long> orderIds = cursorResponse.content().stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        // 리뷰 존재 여부 조회
        Set<Long> reviewedOrderIds = reviewRepository.findReviewedOrderIds(orderIds);

        return OrderListConverter.toOrderListResponse(cursorResponse, totalElements, itemsByOrderId, reviewedOrderIds);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse.OrderDetailDTO getOrderDetail(Long memberId, Long orderId) {

        Order order = orderRepository.findByIdAndMemberIdWithItems(orderId, memberId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findReadOnlyByOrderId(order.getOrderId()).orElse(null);

        List<Long> orderItemIds = order.getOrderItems().stream().map(OrderItem::getId).toList();
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
                        (existing, replacement) -> existing // 혹시 모를 중복 키 방어
                ));

        return OrderDetailConverter.toOrderDetailDTO(order, payment, optionsByOrderItemId, menuImageUrls);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderCancelResponse cancelOrder(Long memberId, Long orderId, OrderCancelRequest request) {
        OrderCancelPreparation preparation = orderCancelTransactionService.prepareCustomerCancel(memberId, orderId);

        PaymentCancelResult paymentCancelResult = paymentCancelService.cancel(
                preparation.payment(),
                request.cancelReason(),
                preparation.refundAmount()
        );

        try {
            return orderCancelTransactionService.cancelCustomerOrder(
                    memberId,
                    orderId,
                    request.cancelReason(),
                    preparation.refundRate(),
                    preparation.refundAmount(),
                    paymentCancelResult
            );
        } catch (RuntimeException e) {
            logPaymentCancelPersistenceFailure(
                    "customer cancel",
                    orderId,
                    preparation.payment(),
                    paymentCancelResult,
                    e
            );
            throw e;
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderStatusChangeResponse acceptOrder(Long ownerId, MemberType memberType, Long orderId) {
        validateBusinessMember(memberType);
        return orderOwnerActionTransactionService.acceptOrder(ownerId, orderId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderStatusChangeResponse rejectOrder(Long ownerId, MemberType memberType, Long orderId) {
        validateBusinessMember(memberType);

        OrderRejectPreparation preparation = orderOwnerActionTransactionService.prepareRejectOrder(ownerId, orderId);

        PaymentCancelResult paymentCancelResult = paymentCancelService.cancel(
                preparation.payment(),
                OWNER_REJECT_CANCEL_REASON,
                preparation.refundAmount()
        );

        try {
            return orderOwnerActionTransactionService.rejectOrder(
                    ownerId,
                    orderId,
                    preparation.refundAmount(),
                    paymentCancelResult
            );
        } catch (RuntimeException e) {
            logPaymentCancelPersistenceFailure(
                    "owner reject",
                    orderId,
                    preparation.payment(),
                    paymentCancelResult,
                    e
            );
            throw e;
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderStatusChangeResponse completePickup(Long ownerId, MemberType memberType, Long orderId) {
        validateBusinessMember(memberType);
        return orderOwnerActionTransactionService.completePickup(ownerId, orderId);
    }

    private void validateBusinessMember(MemberType memberType) {
        if (memberType == MemberType.BUSINESS) {
            return;
        }

        throw new GeneralException(OrderErrorStatus.BUSINESS_MEMBER_REQUIRED);
    }

    private void logPaymentCancelPersistenceFailure(
            String action,
            Long orderId,
            Payment payment,
            PaymentCancelResult paymentCancelResult,
            RuntimeException exception
    ) {
        if (!paymentCancelResult.canceled()) {
            return;
        }

        log.error(
                "Payment cancel succeeded but order persistence failed. action={}, orderId={}, paymentId={}, paymentKey={}, refundedAmount={}, canceledAt={}, lastTransactionKey={}",
                action,
                orderId,
                payment != null ? payment.getId() : null,
                payment != null ? payment.getPaymentKey() : null,
                paymentCancelResult.refundedAmount(),
                paymentCancelResult.canceledAt(),
                paymentCancelResult.lastTransactionKey(),
                exception
        );
    }
}
