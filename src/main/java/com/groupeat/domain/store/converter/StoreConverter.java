package com.groupeat.domain.store.converter;

import com.groupeat.domain.store.dto.response.OwnerStoreResponse;
import com.groupeat.domain.store.dto.response.StoreDetailResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class StoreConverter {

    public static StoreDetailResponse toStoreDetailResponse(Store store, StoreOrderSchedule schedule) {

        return StoreDetailResponse.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .reviewRating(store.getReviewRating())
                .reviewCount(store.getReviewCount())
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .closedDays(toClosedDays(schedule))
                .pickupOpenTime(toEarliestPickupOpenTime(schedule))
                .pickupCloseTime(toLatestPickupCloseTime(schedule))
                .minOrderDays(schedule != null ? schedule.getMinOrderDays() : null)
                .discountConditionQuantity(store.getDiscountConditionQuantity())
                .discountRate(store.getDiscountRate())
                .orderProcess(store.getOrderProcess())
                .minOrderQuantity(toMinOrderQuantity(schedule))
                .maxOrderQuantity(toMaxOrderQuantity(schedule))
                .scheduleStartDate(schedule != null ? schedule.getStartDate() : null)
                .scheduleEndDate(schedule != null ? schedule.getEndDate() : null)
                .build();
    }

    public static OwnerStoreResponse toOwnerStoreResponse(Store store) {

        return OwnerStoreResponse.builder()
                .storeId(store.getId())
                .imageUrl(store.getImageUrl())
                .storeName(store.getStoreName())
                .location(toLocationDTO(store))
                .category(store.getCategory())
                .categoryName(store.getCategory() != null ? store.getCategory().getDescription() : null)
                .phoneNumber(store.getPhoneNumber())
                .description(store.getDescription())
                .discount(toDiscountDTO(store))
                .build();
    }

    private static OwnerStoreResponse.LocationDTO toLocationDTO(Store store) {
        return OwnerStoreResponse.LocationDTO.builder()
                .address(store.getAddress())
                .district(store.getDistrict() != null ? store.getDistrict() : getDistrictDescription(store))
                .neighborhood(store.getNeighborhood())
                .detailAddress(store.getDetailAddress())
                .build();
    }

    private static OwnerStoreResponse.DiscountDTO toDiscountDTO(Store store) {
        return OwnerStoreResponse.DiscountDTO.builder()
                .conditionQuantity(store.getDiscountConditionQuantity())
                .rate(store.getDiscountRate())
                .build();
    }

    private static String getDistrictDescription(Store store) {
        return store.getDistrict() != null ? store.getDistrict() : null;
    }

    private static String toClosedDays(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        String closedDays = schedule.getDays().stream()
                .filter(day -> !day.isAvailable())
                .sorted(Comparator.comparing(StoreOrderScheduleDay::getDayOfWeek))
                .map(day -> day.getDayOfWeek().name())
                .collect(Collectors.joining(","));

        return closedDays.isBlank() ? null : closedDays;
    }

    private static LocalTime toEarliestPickupOpenTime(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        return schedule.getDays().stream()
                .filter(StoreOrderScheduleDay::isAvailable)
                .map(StoreOrderScheduleDay::getPickupStartTime)
                .filter(Objects::nonNull)
                .min(LocalTime::compareTo)
                .orElse(null);
    }

    private static LocalTime toLatestPickupCloseTime(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        return schedule.getDays().stream()
                .filter(StoreOrderScheduleDay::isAvailable)
                .map(StoreOrderScheduleDay::getPickupEndTime)
                .filter(Objects::nonNull)
                .max(LocalTime::compareTo)
                .orElse(null);
    }

    private static Integer toMinOrderQuantity(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return schedule.getDays().stream()
                .filter(StoreOrderScheduleDay::isAvailable) // 영업하는 요일만 대상
                .map(StoreOrderScheduleDay::getMinOrderQuantity)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);
    }
    
    private static Integer toMaxOrderQuantity(StoreOrderSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        return schedule.getDays().stream()
                .filter(StoreOrderScheduleDay::isAvailable) // 영업하는 요일만 대상
                .map(StoreOrderScheduleDay::getMaxOrderQuantity)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }
}
