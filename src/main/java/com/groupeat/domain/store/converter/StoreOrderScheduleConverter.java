package com.groupeat.domain.store.converter;

import com.groupeat.domain.store.dto.response.OwnerStoreOrderScheduleResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StoreOrderScheduleConverter {

    public static OwnerStoreOrderScheduleResponse toOwnerResponse(Store store, StoreOrderSchedule schedule) {
        if (schedule == null) {
            return OwnerStoreOrderScheduleResponse.builder()
                    .storeId(store.getId())
                    .days(Arrays.stream(DayOfWeek.values())
                            .map(StoreOrderScheduleConverter::toUnavailableDayResponse)
                            .toList())
                    .build();
        }

        Map<DayOfWeek, StoreOrderScheduleDay> dayMap = schedule.getDays().stream()
                .collect(Collectors.toMap(StoreOrderScheduleDay::getDayOfWeek, Function.identity()));

        return OwnerStoreOrderScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .storeId(store.getId())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .minOrderDays(schedule.getMinOrderDays())
                .days(Arrays.stream(DayOfWeek.values())
                        .map(dayOfWeek -> toDayResponse(dayOfWeek, dayMap.get(dayOfWeek)))
                        .toList())
                .build();
    }

    private static OwnerStoreOrderScheduleResponse.DayScheduleResponse toDayResponse(
            DayOfWeek dayOfWeek,
            StoreOrderScheduleDay day
    ) {
        if (day == null) {
            return toUnavailableDayResponse(dayOfWeek);
        }

        return OwnerStoreOrderScheduleResponse.DayScheduleResponse.builder()
                .dayOfWeek(day.getDayOfWeek())
                .available(day.isAvailable())
                .minOrderQuantity(day.getMinOrderQuantity())
                .maxOrderQuantity(day.getMaxOrderQuantity())
                .pickupOpenTime(day.getPickupOpenTime())
                .pickupCloseTime(day.getPickupCloseTime())
                .intervalMinutes(day.getIntervalMinutes())
                .build();
    }

    private static OwnerStoreOrderScheduleResponse.DayScheduleResponse toUnavailableDayResponse(DayOfWeek dayOfWeek) {
        return OwnerStoreOrderScheduleResponse.DayScheduleResponse.builder()
                .dayOfWeek(dayOfWeek)
                .available(false)
                .intervalMinutes(StoreOrderScheduleDay.DEFAULT_INTERVAL_MINUTES)
                .build();
    }
}
