package com.groupeat.domain.search.converter;

import com.groupeat.domain.search.dto.response.StoreSearchResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchConverter {

    public static StoreSearchResponse.StoreListDTO toStoreListDTO(
            List<Store> stores,
            long totalElements,
            Map<Long, StoreOrderSchedule> scheduleMap
    ) {
        List<StoreSearchResponse.StoreCardDTO> storeCards = stores.stream()
                .map(store -> toStoreCardDTO(store, scheduleMap.get(store.getId())))
                .collect(Collectors.toList());

        return StoreSearchResponse.StoreListDTO.builder()
                .totalElements(totalElements)
                .storeList(storeCards)
                .build();
    }

    private static StoreSearchResponse.StoreCardDTO toStoreCardDTO(Store store, StoreOrderSchedule schedule) {
        LocalTime pickupOpenTime = toEarliestPickupOpenTime(schedule);
        LocalTime pickupCloseTime = toLatestPickupCloseTime(schedule);
        String timeRange = (pickupOpenTime != null && pickupCloseTime != null)
                ? pickupOpenTime + " ~ " + pickupCloseTime
                : "시간 정보 없음";

        return StoreSearchResponse.StoreCardDTO.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .name(store.getStoreName())
                .category(store.getCategory() != null ? store.getCategory().getDescription() : null)
                .minPrice(store.getMinPrice())
                .maxPrice(store.getMaxPrice())
                .phoneNumber(store.getPhoneNumber())
                .rating(store.getReviewRating())
                .pickupTimeRange(timeRange)
                .build();
    }

    private static LocalTime toEarliestPickupOpenTime(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        return schedule.getDays().stream()
                .filter(StoreOrderScheduleDay::isAvailable)
                .map(StoreOrderScheduleDay::getPickupOpenTime)
                .min(LocalTime::compareTo)
                .orElse(null);
    }

    private static LocalTime toLatestPickupCloseTime(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        return schedule.getDays().stream()
                .filter(StoreOrderScheduleDay::isAvailable)
                .map(StoreOrderScheduleDay::getPickupCloseTime)
                .max(LocalTime::compareTo)
                .orElse(null);
    }
}
