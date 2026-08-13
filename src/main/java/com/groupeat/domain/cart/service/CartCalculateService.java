package com.groupeat.domain.cart.service;

import com.groupeat.domain.cart.dto.request.CartCalculateRequest;
import com.groupeat.domain.cart.dto.response.CartCalculateResponse;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.cart.exception.CartErrorStatus;
import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
import com.groupeat.domain.store.entity.*;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.MenuOptionRepository;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartCalculateService {

    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final StoreOrderScheduleRepository storeOrderScheduleRepository;

    public CartCalculateResponse calculate(Long memberId, CartCalculateRequest request) {
        List<Long> targetIds = request.cartItemIds();

        if (targetIds == null || targetIds.isEmpty()) {
            throw new GeneralException(CartErrorStatus.EMPTY_CART_SELECTION);
        }

        List<CartItem> cartItems = cartItemRepository.findAllById(targetIds);
        if (cartItems.size() != targetIds.size()) {
            throw new GeneralException(CartErrorStatus.CART_ITEM_NOT_FOUND);
        }

        for (CartItem item : cartItems) {
            if (!item.getCart().getMemberId().equals(memberId)) {
                throw new GeneralException(CartErrorStatus.CART_ITEM_NOT_FOUND);
            }
        }

        List<Long> storeIds = cartItems.stream().map(CartItem::getStoreId).distinct().toList();
        if (storeIds.size() > 1) {
            throw new GeneralException(CartErrorStatus.MULTIPLE_STORE_NOT_ALLOWED);
        }

        // 픽업 날짜/시간이 하나로 일치하는지 검증
        long dateTimeCount = cartItems.stream()
                .map(item -> item.getPickupDate().toString() + "T" + item.getPickupTime().toString())
                .distinct()
                .count();
        if (dateTimeCount > 1) {
            throw new GeneralException(CartErrorStatus.DIFFERENT_PICKUP_TIME);
        }

        Store store = storeRepository.findById(storeIds.get(0))
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        LocalDate pickupDate = cartItems.get(0).getPickupDate();
        LocalTime pickupTime = cartItems.get(0).getPickupTime();

        validateScheduleAndQuantity(store.getId(), pickupDate, pickupTime, totalQuantity);

        Map<Long, Menu> menuMap = menuRepository.findAllById(
                cartItems.stream().map(CartItem::getMenuId).distinct().toList()
        ).stream().collect(Collectors.toMap(Menu::getId, m -> m));

        List<CartItemOption> allOptions = cartItemOptionRepository.findAllByCartItemIdIn(targetIds);
        Map<Long, List<CartItemOption>> optionsMap = allOptions.stream().collect(Collectors.groupingBy(opt -> opt.getCartItem().getId()));

        Map<Long, MenuOption> realOptionsMap = menuOptionRepository.findAllById(
                allOptions.stream().map(CartItemOption::getMenuOptionId).distinct().toList()
        ).stream().collect(Collectors.toMap(MenuOption::getId, o -> o));

        return this.calculateWithEntities(cartItems, store, menuMap, optionsMap, realOptionsMap);
    }

    public CartCalculateResponse calculateWithEntities(
            List<CartItem> cartItems, Store store, Map<Long, Menu> menuMap,
            Map<Long, List<CartItemOption>> optionsMap, Map<Long, MenuOption> realOptionsMap
    ) {
        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();

        // 날짜/시간별로 수량 합산
        Map<String, Integer> quantityByDateTime = cartItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getPickupDate().toString() + "T" + item.getPickupTime().toString(),
                        Collectors.summingInt(CartItem::getQuantity)
                ));

        int totalOriginalPrice = 0;
        int totalDiscountAmount = 0;
        List<CartCalculateResponse.CalculatedItem> calculatedItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Menu menu = menuMap.get(item.getMenuId());
            List<CartItemOption> options = optionsMap.getOrDefault(item.getId(), List.of());

            int unitPrice = menu.getBasePrice() + options.stream()
                    .mapToInt(opt -> realOptionsMap.get(opt.getMenuOptionId()).getAdditionalPrice()).sum();
            int itemOriginalPrice = unitPrice * item.getQuantity();

            // 현재 아이템의 그룹 수량을 기반으로 실제 할인율 도출
            String dateTimeKey = item.getPickupDate().toString() + "T" + item.getPickupTime().toString();
            int totalGroupQuantity = quantityByDateTime.getOrDefault(dateTimeKey, 0);

            int discountRate = (store.getDiscountConditionQuantity() != null
                    && totalGroupQuantity >= store.getDiscountConditionQuantity())
                    ? store.getDiscountRate() : 0;

            int itemDiscountAmount = (itemOriginalPrice * discountRate) / 100;
            int itemFinalPrice = itemOriginalPrice - itemDiscountAmount;

            totalOriginalPrice += itemOriginalPrice;
            totalDiscountAmount += itemDiscountAmount;

            calculatedItems.add(new CartCalculateResponse.CalculatedItem(
                    item.getId(), item.getQuantity(), unitPrice, itemOriginalPrice, itemDiscountAmount, itemFinalPrice
            ));
        }

        return new CartCalculateResponse(
                store.getId(), totalQuantity, totalOriginalPrice, totalDiscountAmount,
                (totalOriginalPrice - totalDiscountAmount), calculatedItems
        );
    }

    private void validateScheduleAndQuantity(Long storeId, LocalDate pickupDate, LocalTime pickupTime, int totalQuantity) {
        if (pickupDate.isBefore(LocalDate.now()) ||
                (pickupDate.isEqual(LocalDate.now()) && pickupTime.isBefore(LocalTime.now()))) {
            throw new GeneralException(CartErrorStatus.PICKUP_TIME_IN_PAST);
        }

        StoreOrderSchedule schedule = storeOrderScheduleRepository
                .findActiveScheduleByStoreIdAndDate(storeId, pickupDate)
                .orElseThrow(() -> new GeneralException(CartErrorStatus.STORE_SCHEDULE_NOT_FOUND));

        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), pickupDate);
        if (daysBetween < schedule.getMinOrderDays()) {
            throw new GeneralException(CartErrorStatus.PICKUP_DATE_BEFORE_LEAD_TIME);
        }

        StoreOrderScheduleDay scheduleDay = schedule.getDays().stream()
                .filter(day -> day.getDayOfWeek() == pickupDate.getDayOfWeek())
                .findFirst()
                .orElseThrow(() -> new GeneralException(CartErrorStatus.STORE_NOT_AVAILABLE_ON_DAY));

        if (!scheduleDay.isAvailable()) {
            throw new GeneralException(CartErrorStatus.STORE_NOT_AVAILABLE_ON_DAY);
        }

        if (pickupTime.isBefore(scheduleDay.getPickupStartTime()) || pickupTime.isAfter(scheduleDay.getPickupEndTime())) {
            throw new GeneralException(CartErrorStatus.STORE_NOT_AVAILABLE_ON_DAY);
        }
        if (scheduleDay.getBreakStartTime() != null && scheduleDay.getBreakEndTime() != null) {
            boolean isDuringBreak = !pickupTime.isBefore(scheduleDay.getBreakStartTime()) && !pickupTime.isAfter(scheduleDay.getBreakEndTime());
            if (isDuringBreak) {
                throw new GeneralException(CartErrorStatus.STORE_NOT_AVAILABLE_ON_DAY);
            }
        }

        if (scheduleDay.getMinOrderQuantity() != null && totalQuantity < scheduleDay.getMinOrderQuantity()) {
            throw new GeneralException(CartErrorStatus.MIN_ORDER_QUANTITY_NOT_SATISFIED);
        }
        if (scheduleDay.getMaxOrderQuantity() != null && totalQuantity > scheduleDay.getMaxOrderQuantity()) {
            throw new GeneralException(CartErrorStatus.MAX_ORDER_QUANTITY_EXCEEDED);
        }
    }
}