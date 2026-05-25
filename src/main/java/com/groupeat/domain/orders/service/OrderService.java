package com.groupeat.domain.orders.service;

import com.groupeat.domain.cart.dto.request.CartCalculateRequest;
import com.groupeat.domain.cart.dto.response.CartCalculateResponse;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
import com.groupeat.domain.cart.service.CartCalculateService;
import com.groupeat.domain.orders.converter.OrderConverter;
import com.groupeat.domain.orders.dto.request.OrderCreateRequest;
import com.groupeat.domain.orders.dto.response.OrderCreateResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.repository.OrderItemOptionRepository;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
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

import java.time.LocalDateTime;
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
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;

    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    private final CartCalculateService cartCalculateService;

    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        List<Long> cartItemIds = request.cartItemIds();

        // 장바구니 계산 로직 재사용 (검증 및 금액 계산)
        CartCalculateResponse calculated = cartCalculateService.calculate(memberId, new CartCalculateRequest(cartItemIds));

        // 스냅샷 저장을 위한 원본 데이터 세팅
        Store store = storeRepository.findById(calculated.storeId())
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
        Map<Long, Menu> menuMap = menuRepository.findAllById(cartItems.stream().map(CartItem::getMenuId).toList())
                .stream().collect(Collectors.toMap(Menu::getId, m -> m));

        List<CartItemOption> allCartItemOptions = cartItemOptionRepository.findAllByCartItemIdIn(cartItemIds);
        Map<Long, List<CartItemOption>> optionsMap = allCartItemOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getCartItem().getId()));

        Map<Long, MenuOption> realOptionsMap = menuOptionRepository.findAllById(allCartItemOptions.stream().map(CartItemOption::getMenuOptionId).toList())
                .stream().collect(Collectors.toMap(MenuOption::getId, o -> o));

        // 결제 금액 & orderId, 픽업시간
        int finalPaymentAmount = (int) (calculated.finalPaymentAmount() * request.paymentMethod().getPaymentRatio());
        String generatedOrderId = "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        LocalDateTime pickupDateTime = cartItems.get(0).getPickupDateTime();

        // Order 먼저 DB에 저장하여 ID 확보
        Order order = OrderConverter.toOrder(
                generatedOrderId, memberId, store,
                calculated.totalOriginalPrice(), calculated.totalDiscountAmount(),
                finalPaymentAmount, pickupDateTime, request
        );
        Order savedOrder = orderRepository.save(order);

        Map<Long, CartCalculateResponse.CalculatedItem> calcMap = calculated.calculatedItems().stream()
                .collect(Collectors.toMap(CartCalculateResponse.CalculatedItem::cartItemId, item -> item));

        List<OrderItemOption> orderItemOptionsToSave = new ArrayList<>();

        // OrderItem 저장
        for (CartItem cartItem : cartItems) {
            Menu menu = menuMap.get(cartItem.getMenuId());
            CartCalculateResponse.CalculatedItem calcItem = calcMap.get(cartItem.getId());

            OrderItem orderItem = OrderConverter.toOrderItem(
                    savedOrder,
                    cartItem, menu, calcItem.unitPrice(),
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

        return OrderConverter.toOrderCreateResponse(savedOrder);
    }
}