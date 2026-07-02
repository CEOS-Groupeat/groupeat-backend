package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.dto.request.OwnerStoreOrderScheduleRequest;
import com.groupeat.domain.store.dto.response.OwnerStoreOrderScheduleResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
import com.groupeat.domain.store.enums.StoreCategory;
import com.groupeat.domain.store.enums.StoreRegion;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OwnerStoreOrderScheduleServiceTest {

    private static final Long BUSINESS_MEMBER_ID = 2L;

    private StoreRepository storeRepository;
    private StoreOrderScheduleRepository scheduleRepository;
    private OwnerStoreOrderScheduleService ownerStoreOrderScheduleService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        scheduleRepository = mock(StoreOrderScheduleRepository.class);
        ownerStoreOrderScheduleService = new OwnerStoreOrderScheduleService(storeRepository, scheduleRepository);
    }

    @Test
    void getMyOrderSchedule_withoutSchedule_returnsEmptySevenDays() {
        Store store = store();
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(scheduleRepository.findFirstByStore_OwnerIdAndDeletedAtIsNullOrderByStartDateDesc(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.empty());

        OwnerStoreOrderScheduleResponse response = ownerStoreOrderScheduleService.getMyOrderSchedule(activeBusinessMember());

        assertThat(response.storeId()).isEqualTo(store.getId());
        assertThat(response.scheduleId()).isNull();
        assertThat(response.days()).hasSize(7);
        assertThat(response.days()).allSatisfy(day -> {
            assertThat(day.available()).isFalse();
            assertThat(day.intervalMinutes()).isEqualTo(30);
        });
    }

    @Test
    void saveMyOrderSchedule_createsScheduleAndTreatsMissingDaysAsUnavailable() {
        Store store = store();
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(scheduleRepository.findFirstByStore_OwnerIdAndDeletedAtIsNullOrderByStartDateDesc(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.empty());
        when(scheduleRepository.save(any(StoreOrderSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OwnerStoreOrderScheduleResponse response = ownerStoreOrderScheduleService.saveMyOrderSchedule(
                activeBusinessMember(),
                requestWithMondayAvailable()
        );

        assertThat(response.storeId()).isEqualTo(store.getId());
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 5, 20));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2027, 5, 20));
        assertThat(response.minOrderDays()).isEqualTo(3);
        assertThat(response.days()).hasSize(7);
        assertThat(response.days())
                .filteredOn(day -> day.dayOfWeek() == DayOfWeek.MONDAY)
                .singleElement()
                .satisfies(day -> {
                    assertThat(day.available()).isTrue();
                    assertThat(day.minOrderQuantity()).isEqualTo(10);
                    assertThat(day.maxOrderQuantity()).isEqualTo(100);
                    assertThat(day.pickupOpenTime()).isEqualTo(LocalTime.of(10, 0));
                    assertThat(day.pickupCloseTime()).isEqualTo(LocalTime.of(17, 0));
                    assertThat(day.intervalMinutes()).isEqualTo(30);
                });
        assertThat(response.days())
                .filteredOn(day -> day.dayOfWeek() == DayOfWeek.TUESDAY)
                .singleElement()
                .satisfies(day -> assertThat(day.available()).isFalse());
        verify(scheduleRepository).save(any(StoreOrderSchedule.class));
    }

    @Test
    void saveMyOrderSchedule_updatesExistingSchedule() {
        Store store = store();
        StoreOrderSchedule existingSchedule = existingSchedule(store);
        Map<DayOfWeek, StoreOrderScheduleDay> existingDays = existingSchedule.getDays().stream()
                .collect(Collectors.toMap(StoreOrderScheduleDay::getDayOfWeek, Function.identity()));
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));
        when(scheduleRepository.findFirstByStore_OwnerIdAndDeletedAtIsNullOrderByStartDateDesc(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(existingSchedule));

        OwnerStoreOrderScheduleResponse response = ownerStoreOrderScheduleService.saveMyOrderSchedule(
                activeBusinessMember(),
                requestWithMondayAvailable()
        );

        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 5, 20));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2027, 5, 20));
        assertThat(response.minOrderDays()).isEqualTo(3);
        assertThat(existingSchedule.getDays()).hasSize(7);
        assertThat(existingSchedule.getDays()).allSatisfy(day ->
                assertThat(day).isSameAs(existingDays.get(day.getDayOfWeek()))
        );
        assertThat(existingDays.get(DayOfWeek.MONDAY).isAvailable()).isTrue();
        assertThat(existingDays.get(DayOfWeek.TUESDAY).isAvailable()).isFalse();
        verify(scheduleRepository, never()).save(any(StoreOrderSchedule.class));
    }

    @Test
    void saveMyOrderSchedule_duplicateDay_throwsInvalidSchedule() {
        Store store = store();
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.of(store));

        OwnerStoreOrderScheduleRequest request = OwnerStoreOrderScheduleRequest.builder()
                .startDate(LocalDate.of(2026, 5, 20))
                .endDate(LocalDate.of(2027, 5, 20))
                .minOrderDays(3)
                .days(List.of(availableMonday(), availableMonday()))
                .build();

        assertThatThrownBy(() -> ownerStoreOrderScheduleService.saveMyOrderSchedule(activeBusinessMember(), request))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.INVALID_ORDER_SCHEDULE)
                );
    }

    @Test
    void saveMyOrderSchedule_customerMember_throwsBusinessRequired() {
        AuthenticatedMember member = new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.CUSTOMER,
                MemberStatus.ACTIVE,
                false
        );

        assertThatThrownBy(() -> ownerStoreOrderScheduleService.saveMyOrderSchedule(member, requestWithMondayAvailable()))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED)
                );
    }

    @Test
    void saveMyOrderSchedule_withoutOwnedStore_throwsOwnerStoreNotFound() {
        when(storeRepository.findActiveStoreByBusinessMemberId(BUSINESS_MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerStoreOrderScheduleService.saveMyOrderSchedule(
                activeBusinessMember(),
                requestWithMondayAvailable()
        ))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.OWNER_STORE_NOT_FOUND)
                );
    }

    private AuthenticatedMember activeBusinessMember() {
        return new AuthenticatedMember(
                BUSINESS_MEMBER_ID,
                MemberType.BUSINESS,
                MemberStatus.ACTIVE,
                false
        );
    }

    private OwnerStoreOrderScheduleRequest requestWithMondayAvailable() {
        return OwnerStoreOrderScheduleRequest.builder()
                .startDate(LocalDate.of(2026, 5, 20))
                .endDate(LocalDate.of(2027, 5, 20))
                .minOrderDays(3)
                .days(List.of(availableMonday()))
                .build();
    }

    private OwnerStoreOrderScheduleRequest.DayScheduleRequest availableMonday() {
        return OwnerStoreOrderScheduleRequest.DayScheduleRequest.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .available(true)
                .minOrderQuantity(10)
                .maxOrderQuantity(100)
                .pickupOpenTime(LocalTime.of(10, 0))
                .pickupCloseTime(LocalTime.of(17, 0))
                .intervalMinutes(30)
                .build();
    }

    private StoreOrderSchedule existingSchedule(Store store) {
        return StoreOrderSchedule.create(
                store,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                1,
                Arrays.stream(DayOfWeek.values())
                        .map(StoreOrderScheduleDay::createUnavailable)
                        .toList()
        );
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
                .discountConditionQuantity(50)
                .discountRate(5)
                .minPrice(5000)
                .maxPrice(20000)
                .build();
    }
}
