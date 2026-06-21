package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.dto.request.OwnerStoreUpdateRequest;
import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OwnerStoreServiceTest {

    private static final Long BUSINESS_MEMBER_ID = 2L;

    private StoreRepository storeRepository;
    private OwnerStoreService ownerStoreService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        ownerStoreService = new OwnerStoreService(storeRepository);
    }

    @Test
    // 로그인한 활성 사업자 회원의 가게 정보를 조회
    void getMyStore_returnsOwnedStore() {
        Store store = store();
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));

        OwnerStoreResponse response = ownerStoreService.getMyStore(activeBusinessMember());

        assertThat(response.storeId()).isEqualTo(1L);
        assertThat(response.storeName()).isEqualTo("데이브런치");
        assertThat(response.category()).isEqualTo(StoreCategory.SANDWICH_KIMBAP);
        assertThat(response.categoryName()).isEqualTo("샌드위치&김밥");
        assertThat(response.location().address()).isEqualTo("마포구 00로 00길");
        assertThat(response.location().district()).isEqualTo("마포구");
        assertThat(response.location().neighborhood()).isEqualTo("서교동");
        assertThat(response.location().detailAddress()).isEqualTo("00로 00길 12, 3층");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/store-main.jpg");
        assertThat(response.phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.discount().conditionQuantity()).isEqualTo(50);
        assertThat(response.discount().rate()).isEqualTo(5);
    }

    @Test
    // 일반 회원이 내 가게 조회를 요청하면 예외가 발생
    void getMyStore_customerMember_throwsBusinessRequired() {
        AuthenticatedMember member = new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.CUSTOMER,
                MemberStatus.ACTIVE
        );

        assertThatThrownBy(() -> ownerStoreService.getMyStore(member))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED)
                );
    }

    @Test
    // 활성 상태가 아닌 사업자 회원이 요청하면 예외가 발생한다.
    void getMyStore_inactiveBusinessMember_throwsActiveBusinessRequired() {
        AuthenticatedMember member = new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.BUSINESS,
                MemberStatus.BUSINESS_PENDING
        );

        assertThatThrownBy(() -> ownerStoreService.getMyStore(member))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.ACTIVE_BUSINESS_MEMBER_REQUIRED)
                );
    }

    @Test
    // 사업자 회원에게 연결된 활성 가게가 없으면 예외가 발생한다.
    void getMyStore_withoutOwnedStore_throwsOwnerStoreNotFound() {
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerStoreService.getMyStore(activeBusinessMember()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.OWNER_STORE_NOT_FOUND)
                );
    }

    @Test
    // 로그인한 활성 사업자 회원의 가게 정보를 수정한다.
    void updateMyStore_updatesOwnedStore() {
        Store store = store();
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));

        OwnerStoreResponse response = ownerStoreService.updateMyStore(activeBusinessMember(), updateRequest());

        assertThat(response.storeId()).isEqualTo(1L);
        assertThat(response.storeName()).isEqualTo("브런치하우스");
        assertThat(response.imageUrl()).isEqualTo("https://example.com/updated-store.jpg");
        assertThat(response.category()).isEqualTo(StoreCategory.DESSERT);
        assertThat(response.categoryName()).isEqualTo("디저트");
        assertThat(response.location().address()).isEqualTo("서울특별시 마포구 와우산로 12");
        assertThat(response.location().district()).isEqualTo("마포구");
        assertThat(response.location().neighborhood()).isEqualTo("상수동");
        assertThat(response.location().detailAddress()).isEqualTo("2층");
        assertThat(response.phoneNumber()).isEqualTo("02-123-4567");
        assertThat(response.description()).isEqualTo("수정된 가게 소개입니다.");
        assertThat(response.discount().conditionQuantity()).isEqualTo(30);
        assertThat(response.discount().rate()).isEqualTo(10);
    }

    @Test
    // 수정할 사업자 회원의 활성 가게가 없으면 예외가 발생한다.
    void updateMyStore_withoutOwnedStore_throwsOwnerStoreNotFound() {
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerStoreService.updateMyStore(activeBusinessMember(), updateRequest()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.OWNER_STORE_NOT_FOUND)
                );
    }

    @Test
    // 일반 회원이 내 가게 수정을 요청하면 예외가 발생한다.
    void updateMyStore_customerMember_throwsBusinessRequired() {
        AuthenticatedMember member = new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.CUSTOMER,
                MemberStatus.ACTIVE
        );

        assertThatThrownBy(() -> ownerStoreService.updateMyStore(member, updateRequest()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED)
                );
    }

    @Test
    // 활성 상태가 아닌 사업자 회원이 내 가게 수정을 요청하면 예외가 발생한다.
    void updateMyStore_inactiveBusinessMember_throwsActiveBusinessRequired() {
        AuthenticatedMember member = new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.BUSINESS,
                MemberStatus.BUSINESS_PENDING
        );

        assertThatThrownBy(() -> ownerStoreService.updateMyStore(member, updateRequest()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.ACTIVE_BUSINESS_MEMBER_REQUIRED)
                );
    }

    private AuthenticatedMember activeBusinessMember() {
        return new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.BUSINESS,
                MemberStatus.ACTIVE
        );
    }

    private OwnerStoreUpdateRequest updateRequest() {
        return OwnerStoreUpdateRequest.builder()
                .storeName("브런치하우스")
                .imageUrl("https://example.com/updated-store.jpg")
                .location(OwnerStoreUpdateRequest.LocationDTO.builder()
                        .address("서울특별시 마포구 와우산로 12")
                        .district("마포구")
                        .neighborhood("상수동")
                        .detailAddress("2층")
                        .build())
                .category(StoreCategory.DESSERT)
                .phoneNumber("02-123-4567")
                .description("수정된 가게 소개입니다.")
                .discount(OwnerStoreUpdateRequest.DiscountDTO.builder()
                        .conditionQuantity(30)
                        .rate(10)
                        .build())
                .build();
    }

    private Store store() {
        Store store = Store.builder()
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
        return store;
    }
}
