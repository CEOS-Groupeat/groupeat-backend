package com.groupeat.domain.store.service;

import com.groupeat.domain.store.dto.response.PickupTimeResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreServiceTest {

    private static final Long STORE_ID = 1L;

    private StoreRepository storeRepository;
    private StoreOrderScheduleRepository storeOrderScheduleRepository;
    private StoreService storeService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        storeOrderScheduleRepository = mock(StoreOrderScheduleRepository.class);
        storeService = new StoreService(storeRepository, storeOrderScheduleRepository);
    }

    @Test
    void getStoreInfo_returnsScheduleSummary() {
        Store store = store();
        StoreOrderSchedule schedule = schedule(
                store,
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2027, 5, 20),
                3,
                List.of(
                        StoreOrderScheduleDay.createAvailable(
                                DayOfWeek.MONDAY,
                                10,
                                100,
                                LocalTime.of(10, 0),
                                LocalTime.of(17, 0),
                                null,
                                null
                        ),
                        StoreOrderScheduleDay.createAvailable(
                                DayOfWeek.WEDNESDAY,
                                10,
                                100,
                                LocalTime.of(9, 0),
                                LocalTime.of(18, 0),
                                null,
                                null
                        ),
                        StoreOrderScheduleDay.createUnavailable(DayOfWeek.TUESDAY)
                )
        );
        when(storeRepository.findActiveStoreById(STORE_ID)).thenReturn(Optional.of(store));
        when(storeOrderScheduleRepository.findFirstByStore_IdAndDeletedAtIsNullOrderByStartDateDesc(STORE_ID))
                .thenReturn(Optional.of(schedule));

        StoreDetailResponse response = storeService.getStoreInfo(STORE_ID);

        assertThat(response.storeId()).isEqualTo(STORE_ID);
        assertThat(response.closedDays()).isEqualTo("TUESDAY");
        assertThat(response.pickupOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.pickupCloseTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.minOrderDays()).isEqualTo(3);
    }

    @Test
    void getAvailablePickupTimes_returnsScheduleForAvailableDay() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.MONDAY).plusWeeks(1);
        Store store = store();
        StoreOrderSchedule schedule = schedule(
                store,
                pickupDate.minusDays(1),
                pickupDate.plusDays(1),
                3,
                List.of(StoreOrderScheduleDay.createAvailable(
                        DayOfWeek.MONDAY,
                        10,
                        100,
                        LocalTime.of(10, 0),
                        LocalTime.of(17, 0),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 0)
                ))
        );
        when(storeRepository.findActiveStoreById(STORE_ID)).thenReturn(Optional.of(store));
        when(storeOrderScheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule));

        PickupTimeResponse response = storeService.getAvailablePickupTimes(STORE_ID, pickupDate);

        assertThat(response.date()).isEqualTo(pickupDate);
        assertThat(response.dailyAvailableQuantity()).isEqualTo(100);
        assertThat(response.intervalMinutes()).isEqualTo(30);
        assertThat(response.pickupTimeRanges()).extracting("startTime", "endTime")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LocalTime.of(10, 0), LocalTime.of(17, 0)));
        assertThat(response.breakTimeRanges()).extracting("startTime", "endTime")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(LocalTime.of(10, 30), LocalTime.of(11, 0)));
    }

    @Test
    void getAvailablePickupTimes_withoutSchedule_returnsUnavailable() {
        LocalDate pickupDate = LocalDate.now().plusDays(7);
        Store store = store();
        when(storeRepository.findActiveStoreById(STORE_ID)).thenReturn(Optional.of(store));
        when(storeOrderScheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.empty());

        PickupTimeResponse response = storeService.getAvailablePickupTimes(STORE_ID, pickupDate);

        assertThat(response.date()).isEqualTo(pickupDate);
        assertThat(response.dailyAvailableQuantity()).isZero();
        assertThat(response.intervalMinutes()).isNull();
        assertThat(response.pickupTimeRanges()).isEmpty();
        assertThat(response.breakTimeRanges()).isEmpty();
    }

    @Test
    void getAvailablePickupTimes_unavailableDay_returnsUnavailable() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.TUESDAY).plusWeeks(1);
        Store store = store();
        StoreOrderSchedule schedule = schedule(
                store,
                pickupDate.minusDays(1),
                pickupDate.plusDays(1),
                3,
                List.of(StoreOrderScheduleDay.createUnavailable(DayOfWeek.TUESDAY))
        );
        when(storeRepository.findActiveStoreById(STORE_ID)).thenReturn(Optional.of(store));
        when(storeOrderScheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule));

        PickupTimeResponse response = storeService.getAvailablePickupTimes(STORE_ID, pickupDate);

        assertThat(response.dailyAvailableQuantity()).isZero();
        assertThat(response.pickupTimeRanges()).isEmpty();
        assertThat(response.breakTimeRanges()).isEmpty();
    }

    @Test
    void getAvailablePickupTimes_beforeMinimumOrderDeadline_returnsUnavailable() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.MONDAY);
        Store store = store();
        StoreOrderSchedule schedule = schedule(
                store,
                pickupDate.minusDays(1),
                pickupDate.plusDays(1),
                10,
                List.of(StoreOrderScheduleDay.createAvailable(
                        DayOfWeek.MONDAY,
                        10,
                        100,
                        LocalTime.of(10, 0),
                        LocalTime.of(17, 0),
                        null,
                        null
                ))
        );
        when(storeRepository.findActiveStoreById(STORE_ID)).thenReturn(Optional.of(store));
        when(storeOrderScheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule));

        PickupTimeResponse response = storeService.getAvailablePickupTimes(STORE_ID, pickupDate);

        assertThat(response.dailyAvailableQuantity()).isZero();
        assertThat(response.pickupTimeRanges()).isEmpty();
        assertThat(response.breakTimeRanges()).isEmpty();
    }

    @Test
    void getAvailablePickupTimes_storeNotFound_throwsStoreNotFound() {
        LocalDate pickupDate = LocalDate.now().plusDays(7);
        when(storeRepository.findActiveStoreById(STORE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getAvailablePickupTimes(STORE_ID, pickupDate))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(StoreErrorStatus.STORE_NOT_FOUND)
                );
    }

    private LocalDate nextOrSame(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }

    private StoreOrderSchedule schedule(
            Store store,
            LocalDate startDate,
            LocalDate endDate,
            Integer minOrderDays,
            List<StoreOrderScheduleDay> days
    ) {
        return StoreOrderSchedule.create(store, startDate, endDate, minOrderDays, days);
    }

    private Store store() {
        return Store.builder()
                .id(STORE_ID)
                .ownerId(2L)
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
