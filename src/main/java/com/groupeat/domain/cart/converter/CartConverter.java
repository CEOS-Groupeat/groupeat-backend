package com.groupeat.domain.cart.converter;

import com.groupeat.domain.cart.dto.request.CartItemAddRequest;
import com.groupeat.domain.cart.dto.response.CartListResponse;
import com.groupeat.domain.cart.entity.Cart;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.MenuOption;
import com.groupeat.domain.store.entity.Store;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartConverter {

    public static CartListResponse toCartListResponse(
            List<CartItem> cartItems, Map<Long, Store> storeMap, Map<Long, Menu> menuMap,
            Map<Long, List<CartItemOption>> cartItemOptionsMap, Map<Long, MenuOption> menuOptionMap
    ) {
        Map<Long, List<CartItem>> itemsByStore = cartItems.stream()
                .collect(Collectors.groupingBy(CartItem::getStoreId, LinkedHashMap::new, Collectors.toList()));

        List<CartListResponse.StoreCartDTO> storeCarts = itemsByStore.entrySet().stream().map(entry -> {
            Long storeId = entry.getKey();
            List<CartItem> storeItems = entry.getValue();
            Store store = storeMap.get(storeId);

            // 가게 내의 아이템들을 '픽업 날짜+시간'을 기준으로 수량  합산
            Map<String, Integer> quantityByDateTime = storeItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getPickupDate().toString() + "T" + item.getPickupTime().toString(),
                            Collectors.summingInt(CartItem::getQuantity)
                    ));

            List<CartListResponse.CartItemDTO> itemDTOs = storeItems.stream()
                    .filter(item -> menuMap.get(item.getMenuId()) != null)
                    .map(item -> {
                        Menu menu = menuMap.get(item.getMenuId());
                        List<CartItemOption> options = cartItemOptionsMap.getOrDefault(item.getId(), List.of());

                        // 현재 순회 중인 아이템의 '픽업 날짜+시간' 키 생성
                        String dateTimeKey = item.getPickupDate().toString() + "T" + item.getPickupTime().toString();

                        // 위에서 미리 계산해둔 해당 시간대의 총 수량 가져오기
                        int totalDateTimeQuantity = quantityByDateTime.getOrDefault(dateTimeKey, 0);

                        // 해당 시간대의 총 수량이 할인 조건을 만족하는지 판별하여 할인율 적용
                        int discountRate = (store != null
                                && store.getDiscountConditionQuantity() != null
                                && store.getDiscountRate() != null
                                && totalDateTimeQuantity >= store.getDiscountConditionQuantity())
                                ? store.getDiscountRate() : 0;

                        return buildCartItemDTO(item, menu, options, menuOptionMap, discountRate);
                    }).toList();

            int storeTotalPrice = itemDTOs.stream().mapToInt(CartListResponse.CartItemDTO::finalPrice).sum();

            return CartListResponse.StoreCartDTO.builder()
                    .storeId(storeId)
                    .storeName(store != null ? store.getStoreName() : "알 수 없는 가게")
                    .storeCategory(store != null && store.getCategory() != null ? store.getCategory().getDescription() : null)
                    .cartItems(itemDTOs)
                    .storeTotalPrice(storeTotalPrice)
                    .build();
        }).toList();

        return CartListResponse.builder().storeCarts(storeCarts).build();
    }

    public static CartItem toCartItem(Cart cart, CartItemAddRequest request) {
        return CartItem.builder()
                .cart(cart)
                .storeId(request.storeId())
                .menuId(request.menuId())
                .quantity(request.quantity())
                .pickupDate(request.pickupDate())
                .pickupTime(request.pickupTime())
                .build();
    }

    public static List<CartItemOption> toCartItemOptions(CartItem cartItem, List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }

        return optionIds.stream()
                .map(optionId -> CartItemOption.builder()
                        .cartItem(cartItem)
                        .menuOptionId(optionId)
                        .build())
                .toList();
    }

    private static CartListResponse.CartItemDTO buildCartItemDTO(
            CartItem item, Menu menu, List<CartItemOption> options,
            Map<Long, MenuOption> menuOptionMap, int discountRate
    ) {
        int unitPrice = menu.getBasePrice();
        List<String> optionNames = List.of();

        if (!options.isEmpty()) {
            List<MenuOption> resolvedOptions = options.stream()
                    .map(opt -> menuOptionMap.get(opt.getMenuOptionId())).toList();

            unitPrice += resolvedOptions.stream().mapToInt(MenuOption::getAdditionalPrice).sum();
            optionNames = resolvedOptions.stream().map(MenuOption::getName).toList();
        }

        int originalTotal = unitPrice * item.getQuantity();
        int discountAmount = (int) (originalTotal * (discountRate / 100.0));

        return CartListResponse.CartItemDTO.builder()
                .cartItemId(item.getId())
                .menuName(menu.getName())
                .optionNames(optionNames)
                .imageUrl(menu.getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .discountAmount(discountAmount)
                .discountRate(discountRate)
                .finalPrice(originalTotal - discountAmount)
                .build();
    }
}
