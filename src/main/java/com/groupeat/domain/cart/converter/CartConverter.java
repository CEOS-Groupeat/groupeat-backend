package com.groupeat.domain.cart.converter;

import com.groupeat.domain.cart.dto.response.CartListResponse;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartConverter {

    public static CartListResponse toCartListResponse(
            List<CartItem> cartItems, Map<Long, Store> storeMap, Map<Long, Menu> menuMap,
            Map<Long, List<CartItemOption>> cartItemOptionsMap, Map<Long, MenuOption> menuOptionMap
    ) {
        Map<Long, List<CartItem>> itemsByStore = cartItems.stream()
                .collect(Collectors.groupingBy(CartItem::getStoreId));

        List<CartListResponse.StoreCartDTO> storeCarts = itemsByStore.entrySet().stream().map(entry -> {
            Long storeId = entry.getKey();
            List<CartItem> storeItems = entry.getValue();
            Store store = storeMap.get(storeId);

            int totalStoreQuantity = storeItems.stream().mapToInt(CartItem::getQuantity).sum();
            int discountRate = (store != null && store.getDiscountConditionQuantity() != null
                    && totalStoreQuantity >= store.getDiscountConditionQuantity())
                    ? store.getDiscountRate() : 0;

            List<CartListResponse.CartItemDTO> itemDTOs = storeItems.stream().map(item -> {
                Menu menu = menuMap.get(item.getMenuId());
                List<CartItemOption> options = cartItemOptionsMap.getOrDefault(item.getId(), List.of());

                return buildCartItemDTO(item, menu, options, menuOptionMap, discountRate);
            }).toList();

            int storeTotalPrice = itemDTOs.stream().mapToInt(CartListResponse.CartItemDTO::finalPrice).sum();

            return CartListResponse.StoreCartDTO.builder()
                    .storeId(storeId)
                    .storeName(store != null ? store.getStoreName() : "알 수 없는 가게")
                    .cartItems(itemDTOs)
                    .storeTotalPrice(storeTotalPrice)
                    .build();
        }).toList();

        return CartListResponse.builder().storeCarts(storeCarts).build();
    }

    private static CartListResponse.CartItemDTO buildCartItemDTO(
            CartItem item, Menu menu, List<CartItemOption> options,
            Map<Long, MenuOption> menuOptionMap, int discountRate
    ) {
        int unitPrice = menu.getBasePrice();
        String optionNames = "";

        if (!options.isEmpty()) {
            List<MenuOption> resolvedOptions = options.stream()
                    .map(opt -> menuOptionMap.get(opt.getMenuOptionId())).toList();

            unitPrice += resolvedOptions.stream().mapToInt(MenuOption::getAdditionalPrice).sum();
            optionNames = resolvedOptions.stream().map(MenuOption::getName).collect(Collectors.joining(", "));
        }

        String menuSummary = menu.getName() + (optionNames.isEmpty() ? "" : " (" + optionNames + ")");

        int originalTotal = unitPrice * item.getQuantity();
        int discountAmount = (int) (originalTotal * (discountRate / 100.0));

        return CartListResponse.CartItemDTO.builder()
                .cartItemId(item.getId())
                .menuSummary(menuSummary)
                .imageUrl(menu.getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .discountAmount(discountAmount)
                .discountRate(discountRate)
                .finalPrice(originalTotal - discountAmount)
                .build();
    }
}