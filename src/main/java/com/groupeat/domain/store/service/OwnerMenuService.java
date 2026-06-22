package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.converter.MenuConverter;
import com.groupeat.domain.store.dto.request.OwnerMenuRequest;
import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.dto.response.OwnerMenuResponse;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerMenuService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public MenuListResponse getMyStoreMenus(AuthenticatedMember member) {
        Store store = findMyStore(member);
        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());

        return MenuConverter.toMenuListResponse(menus);
    }

    @Transactional
    public OwnerMenuResponse createMenu(AuthenticatedMember member, OwnerMenuRequest request) {
        Store store = findMyStore(member);
        Menu menu = Menu.builder()
                .store(store)
                .name(request.name())
                .basePrice(request.basePrice())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .build();

        Menu savedMenu = menuRepository.save(menu);
        refreshStoreMenuPriceRange(store);

        return MenuConverter.toOwnerMenuResponse(savedMenu);
    }

    @Transactional
    public OwnerMenuResponse updateMenu(AuthenticatedMember member, Long menuId, OwnerMenuRequest request) {
        Store store = findMyStore(member);
        Menu menu = findOwnedMenu(store.getId(), menuId);

        menu.updateOwnerMenuInfo(
                request.name(),
                request.basePrice(),
                request.description(),
                request.imageUrl()
        );
        refreshStoreMenuPriceRange(store);

        return MenuConverter.toOwnerMenuResponse(menu);
    }

    @Transactional
    public void deleteMenu(AuthenticatedMember member, Long menuId) {
        Store store = findMyStore(member);
        Menu menu = findOwnedMenu(store.getId(), menuId);

        menu.markAsDeleted();
        refreshStoreMenuPriceRange(store);
    }

    private Store findMyStore(AuthenticatedMember member) {
        validateActiveBusinessMember(member);

        return storeRepository.findActiveStoreByBusinessMemberId(member.memberId())
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.OWNER_STORE_NOT_FOUND));
    }

    private Menu findOwnedMenu(Long storeId, Long menuId) {
        return menuRepository.findActiveByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.MENU_NOT_FOUND));
    }

    // 메뉴 변경 후 검색/정렬에 사용하는 가게 가격 범위를 최신 상태로 업데이트
    private void refreshStoreMenuPriceRange(Store store) {
        List<Menu> activeMenus = menuRepository.findAllByStoreId(store.getId());
        if (activeMenus.isEmpty()) {
            store.updateMenuPriceRange(null, null);
            return;
        }

        int minPrice = activeMenus.get(0).getBasePrice();
        int maxPrice = activeMenus.get(0).getBasePrice();

        for (Menu menu : activeMenus) {
            int basePrice = menu.getBasePrice();
            if (basePrice < minPrice) {
                minPrice = basePrice;
            }
            if (basePrice > maxPrice) {
                maxPrice = basePrice;
            }
        }

        store.updateMenuPriceRange(minPrice, maxPrice);
    }

    private void validateActiveBusinessMember(AuthenticatedMember member) {
        if (member.memberType() != MemberType.BUSINESS) {
            throw new GeneralException(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED);
        }

        if (member.memberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(StoreErrorStatus.ACTIVE_BUSINESS_MEMBER_REQUIRED);
        }
    }
}
