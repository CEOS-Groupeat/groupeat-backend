package com.groupeat.domain.store.service;

import com.groupeat.domain.store.converter.StoreConverter;
import com.groupeat.domain.store.dto.response.PickupTimeResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
import com.groupeat.domain.store.entity.StoreOrderScheduleTimeRange;
import com.groupeat.domain.store.enums.StoreOrderScheduleTimeRangeType;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreOrderScheduleRepository storeOrderScheduleRepository;

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
                .map(schedule -> toPickupTimeResponse(schedule, date))
                .orElseGet(() -> unavailablePickupTimeResponse(date));
    }

    private PickupTimeResponse toPickupTimeResponse(StoreOrderSchedule schedule, LocalDate date) {
        if (!isLeadTimeEnough(schedule, date)) {
            return unavailablePickupTimeResponse(date);
        }

        StoreOrderScheduleDay daySchedule = findDaySchedule(schedule, date.getDayOfWeek());
        if (daySchedule == null || !daySchedule.isAvailable()) {
            return unavailablePickupTimeResponse(date);
        }

        return PickupTimeResponse.builder()
                .date(date)
                .dailyAvailableQuantity(daySchedule.getMaxOrderQuantity())
                .intervalMinutes(daySchedule.getIntervalMinutes())
                .pickupTimeRanges(toTimeRangeResponses(daySchedule, StoreOrderScheduleTimeRangeType.PICKUP))
                .breakTimeRanges(toTimeRangeResponses(daySchedule, StoreOrderScheduleTimeRangeType.BREAK))
                .build();
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
                .dailyAvailableQuantity(0)
                .pickupTimeRanges(List.of())
                .breakTimeRanges(List.of())
                .build();
    }

    private List<PickupTimeResponse.TimeRangeResponse> toTimeRangeResponses(
            StoreOrderScheduleDay daySchedule,
            StoreOrderScheduleTimeRangeType type
    ) {
        return daySchedule.getTimeRanges().stream()
                .filter(timeRange -> timeRange.getType() == type)
                .sorted(Comparator.comparing(StoreOrderScheduleTimeRange::getSortOrder))
                .map(timeRange -> PickupTimeResponse.TimeRangeResponse.builder()
                        .startTime(timeRange.getStartTime())
                        .endTime(timeRange.getEndTime())
                        .build())
                .toList();
    }
}
