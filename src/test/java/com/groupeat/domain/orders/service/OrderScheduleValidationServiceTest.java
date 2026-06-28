package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
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

class OrderScheduleValidationServiceTest {

    private static final Long STORE_ID = 1L;

    private StoreOrderScheduleRepository scheduleRepository;
    private OrderRepository orderRepository;
    private OrderScheduleValidationService orderScheduleValidationService;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(StoreOrderScheduleRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderScheduleValidationService = new OrderScheduleValidationService(scheduleRepository, orderRepository);
    }

    @Test
    void validateOrderCreation_validSchedule_passes() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.MONDAY).plusWeeks(1);
        when(scheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule(pickupDate, 3)));
        when(orderRepository.sumOrderItemQuantityByStoreIdAndPickupDateAndStatus(
                STORE_ID,
                pickupDate,
                OrderStatus.ACCEPTED
        )).thenReturn(20L);

        orderScheduleValidationService.validateOrderCreation(
                STORE_ID,
                pickupDate,
                LocalTime.of(10, 30),
                30
        );
    }

    @Test
    void validateOrderCreation_withoutSchedule_throwsScheduleNotAvailable() {
        LocalDate pickupDate = LocalDate.now().plusDays(7);
        when(scheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderScheduleValidationService.validateOrderCreation(
                STORE_ID,
                pickupDate,
                LocalTime.of(10, 0),
                10
        ))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE)
                );
    }

    @Test
    void validateOrderCreation_invalidInterval_throwsScheduleNotAvailable() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.MONDAY).plusWeeks(1);
        when(scheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule(pickupDate, 3)));

        assertThatThrownBy(() -> orderScheduleValidationService.validateOrderCreation(
                STORE_ID,
                pickupDate,
                LocalTime.of(10, 15),
                10
        ))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE)
                );
    }

    @Test
    void validateOrderCreation_quantityOutOfRange_throwsQuantityNotAvailable() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.MONDAY).plusWeeks(1);
        when(scheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule(pickupDate, 3)));

        assertThatThrownBy(() -> orderScheduleValidationService.validateOrderCreation(
                STORE_ID,
                pickupDate,
                LocalTime.of(10, 0),
                101
        ))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(OrderErrorStatus.ORDER_QUANTITY_NOT_AVAILABLE)
                );
    }

    @Test
    void validateOrderCreation_acceptedQuantityExceedsLimit_throwsQuantityNotAvailable() {
        LocalDate pickupDate = nextOrSame(DayOfWeek.MONDAY).plusWeeks(1);
        when(scheduleRepository.findActiveScheduleByStoreIdAndDate(STORE_ID, pickupDate))
                .thenReturn(Optional.of(schedule(pickupDate, 3)));
        when(orderRepository.sumOrderItemQuantityByStoreIdAndPickupDateAndStatus(
                STORE_ID,
                pickupDate,
                OrderStatus.ACCEPTED
        )).thenReturn(80L);

        assertThatThrownBy(() -> orderScheduleValidationService.validateOrderCreation(
                STORE_ID,
                pickupDate,
                LocalTime.of(10, 0),
                30
        ))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(OrderErrorStatus.ORDER_QUANTITY_NOT_AVAILABLE)
                );
    }

    private LocalDate nextOrSame(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }

    private StoreOrderSchedule schedule(LocalDate pickupDate, int minOrderDays) {
        Store store = Store.builder()
                .id(STORE_ID)
                .ownerId(2L)
                .storeName("테스트 가게")
                .address("서울시")
                .phoneNumber("02-1234-5678")
                .build();

        return StoreOrderSchedule.create(
                store,
                pickupDate.minusDays(1),
                pickupDate.plusDays(1),
                minOrderDays,
                List.of(StoreOrderScheduleDay.createAvailable(
                        DayOfWeek.MONDAY,
                        10,
                        100,
                        LocalTime.of(10, 0),
                        LocalTime.of(17, 0),
                        30
                ))
        );
    }
}
