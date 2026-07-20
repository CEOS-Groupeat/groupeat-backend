package com.groupeat.domain.store.service;

import com.groupeat.domain.store.converter.StoreConverter;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.store.dto.response.PickupTimeResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreOrderScheduleRepository storeOrderScheduleRepository;
    private final OrderRepository orderRepository;

    public StoreDetailResponse getStoreInfo(Long storeId) {
        Store store = storeRepository.findActiveStoreById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));
        StoreOrderSchedule schedule = storeOrderScheduleRepository
                .findFirstByStore_IdAndDeletedAtIsNullOrderByStartDateDesc(storeId)
                .orElse(null);

        return StoreConverter.toStoreDetailResponse(store, schedule);
    }

    public PickupTimeResponse getAvailablePickupTimes(Long storeId, LocalDate date) {

        storeRepository.findActiveStoreById(storeId)
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.STORE_NOT_FOUND));

        return storeOrderScheduleRepository.findActiveScheduleByStoreIdAndDate(storeId, date)
                .map(schedule -> toPickupTimeResponse(storeId, schedule, date))
                .orElseGet(() -> unavailablePickupTimeResponse(date));
    }

    private PickupTimeResponse toPickupTimeResponse(Long storeId, StoreOrderSchedule schedule, LocalDate date) {
        if (!isLeadTimeEnough(schedule, date)) {
            return unavailablePickupTimeResponse(date);
        }

        StoreOrderScheduleDay daySchedule = findDaySchedule(schedule, date.getDayOfWeek());
        if (daySchedule == null || !daySchedule.isAvailable()) {
            return unavailablePickupTimeResponse(date);
        }

        int maxOrderQuantity = daySchedule.getMaxOrderQuantity() != null ? daySchedule.getMaxOrderQuantity() : 0;
        int acceptedQuantity = getAcceptedQuantity(storeId, date);
        int remainingQuantity = Math.max(0, maxOrderQuantity - acceptedQuantity);

        return PickupTimeResponse.builder()
                .date(date)
                .dailyMinOrderQuantity(daySchedule.getMinOrderQuantity())
                .dailyAvailableQuantity(daySchedule.getMaxOrderQuantity())
                .dailyAcceptedQuantity(acceptedQuantity)
                .dailyRemainingQuantity(remainingQuantity)
                .intervalMinutes(daySchedule.getIntervalMinutes())
                .pickupTimeRanges(toPickupTimeRangeResponses(daySchedule))
                .breakTimeRanges(toBreakTimeRangeResponses(daySchedule))
                .build();
    }

    private int getAcceptedQuantity(Long storeId, LocalDate date) {
        Long acceptedQuantity = orderRepository.sumOrderItemQuantityByStoreIdAndPickupDateAndStatus(
                storeId,
                date,
                OrderStatus.ACCEPTED
        );
        return acceptedQuantity != null ? acceptedQuantity.intValue() : 0;
    }

    private boolean isLeadTimeEnough(StoreOrderSchedule schedule, LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), date);
        return daysBetween >= schedule.getMinOrderDays();
    }

    private StoreOrderScheduleDay findDaySchedule(StoreOrderSchedule schedule, DayOfWeek dayOfWeek) {
        return schedule.getDays().stream()
                .filter(daySchedule -> daySchedule.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);
    }

    private PickupTimeResponse unavailablePickupTimeResponse(LocalDate date) {
        return PickupTimeResponse.builder()
                .date(date)
                .dailyMinOrderQuantity(0)
                .dailyAvailableQuantity(0)
                .dailyAcceptedQuantity(0)
                .dailyRemainingQuantity(0)
                .pickupTimeRanges(List.of())
                .breakTimeRanges(List.of())
                .build();
    }

    private List<PickupTimeResponse.TimeRangeResponse> toPickupTimeRangeResponses(StoreOrderScheduleDay daySchedule) {
        return toTimeRangeResponses(daySchedule.getPickupStartTime(), daySchedule.getPickupEndTime());
    }

    private List<PickupTimeResponse.TimeRangeResponse> toBreakTimeRangeResponses(StoreOrderScheduleDay daySchedule) {
        return toTimeRangeResponses(daySchedule.getBreakStartTime(), daySchedule.getBreakEndTime());
    }

    private List<PickupTimeResponse.TimeRangeResponse> toTimeRangeResponses(
            java.time.LocalTime startTime,
            java.time.LocalTime endTime
    ) {
        if (startTime == null || endTime == null) {
            return List.of();
        }

        return List.of(PickupTimeResponse.TimeRangeResponse.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build());
    }
}
