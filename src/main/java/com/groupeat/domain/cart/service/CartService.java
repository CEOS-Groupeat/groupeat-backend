package com.groupeat.domain.cart.service;

import com.groupeat.domain.cart.converter.CartConverter;
import com.groupeat.domain.cart.dto.request.CartItemAddRequest;
import com.groupeat.domain.cart.dto.response.CartListResponse;
import com.groupeat.domain.cart.entity.Cart;
import com.groupeat.domain.cart.entity.CartItem;
import com.groupeat.domain.cart.entity.CartItemOption;
import com.groupeat.domain.cart.exception.CartErrorStatus;
import com.groupeat.domain.cart.repository.CartItemOptionRepository;
import com.groupeat.domain.cart.repository.CartItemRepository;
import com.groupeat.domain.cart.repository.CartRepository;
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
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    // 장바구니에 메뉴 담기
    public CartListResponse addCartItem(Long memberId, CartItemAddRequest request) {

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.MENU_NOT_FOUND));

        List<Long> optionIds = request.optionIds() == null ? List.of() : request.optionIds();

        List<MenuOption> selectedOptions = menuOptionRepository.findAllById(optionIds);
        if (selectedOptions.size() != optionIds.size()) {
            throw new GeneralException(StoreErrorStatus.INVALID_MENU_OPTION);
        }

        if (!optionIds.isEmpty()) {
            long validCount = menuOptionRepository.countValidOptions(optionIds, menu.getId());
            if (validCount != optionIds.size()) {
                throw new GeneralException(StoreErrorStatus.INVALID_MENU_OPTION_MAPPING);
            }
        }

        if (!menu.getStore().getId().equals(request.storeId())) {
            throw new GeneralException(StoreErrorStatus.STORE_NOT_FOUND);
        }

        Cart cart = getOrCreateCart(memberId);

        // CartItem 생성 및 저장
        CartItem cartItem = CartConverter.toCartItem(cart, request);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        List<CartItemOption> options = CartConverter.toCartItemOptions(savedCartItem, optionIds);
        if (!options.isEmpty()) {
            cartItemOptionRepository.saveAll(options);
        }
        return getCartList(memberId);
    }

    // 장바구니 목록 조회
    @Transactional(readOnly = true)
    public CartListResponse getCartList(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).orElse(null);
        if (cart == null) return CartListResponse.builder().storeCarts(List.of()).build();

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
        if (cartItems.isEmpty()) return CartListResponse.builder().storeCarts(List.of()).build();

        List<Long> storeIds = cartItems.stream().map(CartItem::getStoreId).distinct().toList();
        List<Long> menuIds = cartItems.stream().map(CartItem::getMenuId).distinct().toList();
        List<Long> cartItemIds = cartItems.stream().map(CartItem::getId).toList();

        Map<Long, Store> storeMap = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, store -> store));

        Map<Long, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));

        // 해당 장바구니 항목들에 속한 모든 옵션 엔티티 조회
        List<CartItemOption> allOptions = cartItemOptionRepository.findAllByCartItemIdIn(cartItemIds);
        Map<Long, List<CartItemOption>> cartItemOptionsMap = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getCartItem().getId()));

        // 옵션 엔티티들이 가리키는 실제 MenuOption 진짜 데이터 조회
        List<Long> menuOptionIds = allOptions.stream().map(CartItemOption::getMenuOptionId).distinct().toList();
        Map<Long, MenuOption> menuOptionMap = menuOptionRepository.findAllById(menuOptionIds).stream()
                .collect(Collectors.toMap(MenuOption::getId, option -> option));

        // 컨버터에 위임하여 최종 조립
        return CartConverter.toCartListResponse(
                cartItems, storeMap, menuMap, cartItemOptionsMap, menuOptionMap
        );
    }

    // 장바구니 메뉴 삭제
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new GeneralException(CartErrorStatus.CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getMemberId().equals(memberId)) {
            throw new GeneralException(CartErrorStatus.CART_ITEM_NOT_FOUND);
        }

        cartItemOptionRepository.deleteAllByCartItemId(cartItemId);

        cartItemRepository.delete(cartItem);
    }

    // 장바구니가 없으면 새로 생성
    private Cart getOrCreateCart(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.builder().memberId(memberId).build()));
    }
}