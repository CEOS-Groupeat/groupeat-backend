package com.groupeat.domain.cart.service;

import com.groupeat.domain.cart.dto.request.CartCalculateRequest;
import com.groupeat.domain.cart.dto.response.CartCalculateResponse;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.cart.exception.CartErrorStatus;
import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
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

        Store store = storeRepository.findById(storeIds.get(0))
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

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
            List<CartItem> cartItems,
            Store store,
            Map<Long, Menu> menuMap,
            Map<Long, List<CartItemOption>> optionsMap,
            Map<Long, MenuOption> realOptionsMap
    ) {
        int totalQuantity = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        int discountRate = (store.getDiscountConditionQuantity() != null && totalQuantity >= store.getDiscountConditionQuantity())
                ? store.getDiscountRate() : 0;

        int totalOriginalPrice = 0;
        int totalDiscountAmount = 0;
        List<CartCalculateResponse.CalculatedItem> calculatedItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Menu menu = menuMap.get(item.getMenuId());
            List<CartItemOption> options = optionsMap.getOrDefault(item.getId(), List.of());

            int unitPrice = menu.getBasePrice() + options.stream()
                    .mapToInt(opt -> realOptionsMap.get(opt.getMenuOptionId()).getAdditionalPrice()).sum();

            int itemOriginalPrice = unitPrice * item.getQuantity();
            int itemDiscountAmount = (int) (itemOriginalPrice * (discountRate / 100.0));
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
}