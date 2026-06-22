package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.dto.request.OwnerMenuRequest;
import com.groupeat.domain.store.dto.response.MenuListResponse;
import com.groupeat.domain.store.dto.response.OwnerMenuResponse;
import com.groupeat.domain.store.entity.Menu;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.MenuRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OwnerMenuServiceTest {

    private static final Long BUSINESS_MEMBER_ID = 2L;

    private StoreRepository storeRepository;
    private MenuRepository menuRepository;
    private OwnerMenuService ownerMenuService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        menuRepository = mock(MenuRepository.class);
        ownerMenuService = new OwnerMenuService(storeRepository, menuRepository);
    }

    @Test
    void getMyStoreMenus_returnsOwnedStoreMenus() {
        Store store = store();
        Menu menu = menu(10L, store, "햄치즈 샌드위치", 7800);
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(menuRepository.findAllByStoreId(store.getId()))
                .thenReturn(List.of(menu));

        MenuListResponse response = ownerMenuService.getMyStoreMenus(activeBusinessMember());

        assertThat(response.menus()).hasSize(1);
        assertThat(response.menus().get(0).menuId()).isEqualTo(10L);
        assertThat(response.menus().get(0).name()).isEqualTo("햄치즈 샌드위치");
        assertThat(response.menus().get(0).basePrice()).isEqualTo(7800);
    }

    @Test
    void createMenu_savesMenuToOwnedStore() {
        Store store = store();
        Menu savedMenu = menu(10L, store, "햄치즈 샌드위치", 7800);
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);
        when(menuRepository.findAllByStoreId(store.getId()))
                .thenReturn(List.of(savedMenu));

        OwnerMenuResponse response = ownerMenuService.createMenu(activeBusinessMember(), request());

        assertThat(response.menuId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("햄치즈 샌드위치");
        assertThat(response.basePrice()).isEqualTo(7800);
        assertThat(store.getMinPrice()).isEqualTo(7800);
        assertThat(store.getMaxPrice()).isEqualTo(7800);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void updateMenu_updatesOwnedMenu() {
        Store store = store();
        Menu menu = menu(10L, store, "기존 메뉴", 7000);
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(menuRepository.findActiveByIdAndStoreId(menu.getId(), store.getId()))
                .thenReturn(Optional.of(menu));
        when(menuRepository.findAllByStoreId(store.getId()))
                .thenReturn(List.of(menu));

        OwnerMenuResponse response = ownerMenuService.updateMenu(activeBusinessMember(), menu.getId(), request());

        assertThat(response.menuId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("햄치즈 샌드위치");
        assertThat(response.basePrice()).isEqualTo(7800);
        assertThat(response.description()).isEqualTo("햄과 치즈가 들어간 샌드위치입니다.");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/menu.jpg");
        assertThat(store.getMinPrice()).isEqualTo(7800);
        assertThat(store.getMaxPrice()).isEqualTo(7800);
    }

    @Test
    void updateMenu_notOwnedMenu_throwsMenuNotFound() {
        Store store = store();
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(menuRepository.findActiveByIdAndStoreId(999L, store.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerMenuService.updateMenu(activeBusinessMember(), 999L, request()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.MENU_NOT_FOUND)
                );
    }

    @Test
    void deleteMenu_marksMenuAsDeleted() {
        Store store = store();
        Menu menu = menu(10L, store, "햄치즈 샌드위치", 7800);
        Menu remainingMenu = menu(11L, store, "참치김밥", 5000);
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(menuRepository.findActiveByIdAndStoreId(menu.getId(), store.getId()))
                .thenReturn(Optional.of(menu));
        when(menuRepository.findAllByStoreId(store.getId()))
                .thenReturn(List.of(remainingMenu));

        ownerMenuService.deleteMenu(activeBusinessMember(), menu.getId());

        assertThat(menu.getDeletedAt()).isNotNull();
        assertThat(store.getMinPrice()).isEqualTo(5000);
        assertThat(store.getMaxPrice()).isEqualTo(5000);
        verify(menuRepository).flush();
    }

    @Test
    void createMenu_customerMember_throwsBusinessRequired() {
        AuthenticatedMember member = new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.CUSTOMER,
                MemberStatus.ACTIVE
        );

        assertThatThrownBy(() -> ownerMenuService.createMenu(member, request()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED)
                );
    }

    @Test
    void createMenu_withoutOwnedStore_throwsOwnerStoreNotFound() {
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerMenuService.createMenu(activeBusinessMember(), request()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.OWNER_STORE_NOT_FOUND)
                );
    }

    private AuthenticatedMember activeBusinessMember() {
        return new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.BUSINESS,
                MemberStatus.ACTIVE
        );
    }

    private OwnerMenuRequest request() {
        return OwnerMenuRequest.builder()
                .name("햄치즈 샌드위치")
                .basePrice(7800)
                .description("햄과 치즈가 들어간 샌드위치입니다.")
                .imageUrl("https://example.com/menu.jpg")
                .build();
    }

    private Menu menu(Long id, Store store, String name, Integer basePrice) {
        return Menu.builder()
                .id(id)
                .store(store)
                .name(name)
                .basePrice(basePrice)
                .description("메뉴 설명")
                .imageUrl("https://example.com/menu.jpg")
                .build();
    }

    private Store store() {
        return Store.builder()
                .id(1L)
                .ownerId(BUSINESS_MEMBER_ID)
                .storeName("데이브런치")
                .address("마포구 00로 00길")
                .district("마포구")
                .neighborhood("서교동")
                .detailAddress("00로 00길 12, 3층")
                .category(StoreCategory.SANDWICH_KIMBAP)
                .region(StoreRegion.MAPO)
                .phoneNumber("010-1234-5678")
                .description("신선한 재료로 당일 제조합니다.")
                .orderProcess("1. 예약 주문 2. 픽업")
                .imageUrl("https://example.com/store-main.jpg")
                .pickupOpenTime(LocalTime.of(10, 0))
                .pickupCloseTime(LocalTime.of(17, 0))
                .closedDays("MONDAY")
                .minOrderDays(3)
                .discountConditionQuantity(50)
                .discountRate(5)
                .minPrice(5000)
                .maxPrice(20000)
                .build();
    }
}
