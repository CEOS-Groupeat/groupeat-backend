package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.converter.StoreOrderScheduleConverter;
import com.groupeat.domain.store.dto.request.OwnerStoreOrderScheduleDayRequest;
import com.groupeat.domain.store.dto.request.OwnerStoreOrderScheduleRequest;
import com.groupeat.domain.store.dto.response.OwnerStoreOrderScheduleResponse;
import com.groupeat.domain.store.entity.Store;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
import com.groupeat.domain.store.entity.StoreOrderScheduleTimeRange;
import com.groupeat.domain.store.exception.StoreErrorStatus;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import com.groupeat.domain.store.repository.StoreRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreOrderScheduleService {

    private final StoreRepository storeRepository;
    private final StoreOrderScheduleRepository scheduleRepository;

    public OwnerStoreOrderScheduleResponse getMyOrderSchedule(AuthenticatedMember member) {
        Store store = findMyStore(member);
        StoreOrderSchedule schedule = scheduleRepository
                .findFirstByStore_OwnerIdAndDeletedAtIsNullOrderByStartDateDesc(member.memberId())
                .orElse(null);

        return StoreOrderScheduleConverter.toOwnerResponse(store, schedule);
    }

    @Transactional
    public OwnerStoreOrderScheduleResponse saveMyOrderScheduleDay(
            AuthenticatedMember member,
            DayOfWeek dayOfWeek,
            OwnerStoreOrderScheduleDayRequest request
    ) {
        Store store = findMyStore(member);
        validateDayRequest(dayOfWeek, request);

        StoreOrderScheduleDay scheduleDay = toScheduleDay(dayOfWeek, request);
        StoreOrderSchedule schedule = scheduleRepository
                .findFirstByStore_OwnerIdAndDeletedAtIsNullOrderByStartDateDesc(member.memberId())
                .orElse(null);

        if (schedule == null) {
            schedule = StoreOrderSchedule.create(
                    store,
                    request.startDate(),
                    request.endDate(),
                    request.minOrderDays(),
                    List.of(scheduleDay)
            );
            schedule = scheduleRepository.save(schedule);
        } else {
            schedule.updatePeriod(
                    request.startDate(),
                    request.endDate(),
                    request.minOrderDays()
            );
            schedule.updateDays(List.of(scheduleDay));
        }

        return StoreOrderScheduleConverter.toOwnerResponse(store, schedule);
    }

    @Transactional
    public OwnerStoreOrderScheduleResponse saveMyOrderSchedule(
            AuthenticatedMember member,
            OwnerStoreOrderScheduleRequest request
    ) {
        Store store = findMyStore(member);
        validateRequest(request);

        List<StoreOrderScheduleDay> days = toScheduleDays(request);
        StoreOrderSchedule schedule = scheduleRepository
                .findFirstByStore_OwnerIdAndDeletedAtIsNullOrderByStartDateDesc(member.memberId())
                .orElse(null);

        if (schedule == null) {
            schedule = StoreOrderSchedule.create(
                    store,
                    request.startDate(),
                    request.endDate(),
                    request.minOrderDays(),
                    days
            );
            schedule = scheduleRepository.save(schedule);
        } else {
            schedule.updatePeriod(
                    request.startDate(),
                    request.endDate(),
                    request.minOrderDays()
            );
            schedule.updateDays(days);
        }

        return StoreOrderScheduleConverter.toOwnerResponse(store, schedule);
    }

    private Store findMyStore(AuthenticatedMember member) {
        validateActiveBusinessMember(member);

        return storeRepository.findActiveStoreByBusinessMemberId(member.memberId())
                .orElseThrow(() -> new GeneralException(StoreErrorStatus.OWNER_STORE_NOT_FOUND));
    }

    private void validateActiveBusinessMember(AuthenticatedMember member) {
        if (member.memberType() != MemberType.BUSINESS) {
            throw new GeneralException(StoreErrorStatus.BUSINESS_MEMBER_REQUIRED);
        }

        if (member.memberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(StoreErrorStatus.ACTIVE_BUSINESS_MEMBER_REQUIRED);
        }
    }

    private void validateRequest(OwnerStoreOrderScheduleRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        Map<DayOfWeek, OwnerStoreOrderScheduleRequest.DayScheduleRequest> requestByDay =
                new EnumMap<>(DayOfWeek.class);

        for (OwnerStoreOrderScheduleRequest.DayScheduleRequest dayRequest : nonNullDays(request.days())) {
            if (dayRequest == null || dayRequest.dayOfWeek() == null || dayRequest.available() == null) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            if (requestByDay.put(dayRequest.dayOfWeek(), dayRequest) != null) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            validateDayRequest(
                    dayRequest.available(),
                    dayRequest.minOrderQuantity(),
                    dayRequest.maxOrderQuantity(),
                    dayRequest.pickupTimeRanges(),
                    dayRequest.breakTimeRanges()
            );
        }
    }

    private void validateDayRequest(DayOfWeek dayOfWeek, OwnerStoreOrderScheduleDayRequest request) {
        if (dayOfWeek == null
                || request == null
                || request.startDate() == null
                || request.endDate() == null
                || request.minOrderDays() == null
                || request.available() == null) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        validateDayRequest(
                request.available(),
                request.minOrderQuantity(),
                request.maxOrderQuantity(),
                request.pickupTimeRanges(),
                request.breakTimeRanges()
        );
    }

    private void validateDayRequest(
            Boolean available,
            Integer minOrderQuantity,
            Integer maxOrderQuantity,
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> pickupTimeRanges,
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> breakTimeRanges
    ) {
        if (!available) {
            return;
        }

        if (minOrderQuantity == null
                || maxOrderQuantity == null
                || pickupTimeRanges == null
                || pickupTimeRanges.isEmpty()) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        if (minOrderQuantity > maxOrderQuantity) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> nonNullPickupTimeRanges =
                nonNullTimeRanges(pickupTimeRanges);
        List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> nonNullBreakTimeRanges =
                nonNullTimeRanges(breakTimeRanges);

        validateTimeRanges(nonNullPickupTimeRanges);
        validateTimeRanges(nonNullBreakTimeRanges);
        validateNoOverlap(nonNullPickupTimeRanges);
        validateNoOverlap(nonNullBreakTimeRanges);
        validateBreakTimesWithinPickupTimes(nonNullPickupTimeRanges, nonNullBreakTimeRanges);
    }

    private List<StoreOrderScheduleDay> toScheduleDays(OwnerStoreOrderScheduleRequest request) {
        Map<DayOfWeek, OwnerStoreOrderScheduleRequest.DayScheduleRequest> requestByDay =
                new EnumMap<>(DayOfWeek.class);

        for (OwnerStoreOrderScheduleRequest.DayScheduleRequest dayRequest : nonNullDays(request.days())) {
            if (dayRequest == null || dayRequest.dayOfWeek() == null || dayRequest.available() == null) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            requestByDay.put(dayRequest.dayOfWeek(), dayRequest);
        }

        List<StoreOrderScheduleDay> days = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            OwnerStoreOrderScheduleRequest.DayScheduleRequest dayRequest = requestByDay.get(dayOfWeek);
            if (dayRequest == null || !dayRequest.available()) {
                days.add(StoreOrderScheduleDay.createUnavailable(dayOfWeek));
                continue;
            }

            days.add(StoreOrderScheduleDay.createAvailable(
                    dayRequest.dayOfWeek(),
                    dayRequest.minOrderQuantity(),
                    dayRequest.maxOrderQuantity(),
                    toTimeRanges(dayRequest.pickupTimeRanges(), dayRequest.breakTimeRanges())
            ));
        }

        return days;
    }

    private StoreOrderScheduleDay toScheduleDay(
            DayOfWeek dayOfWeek,
            OwnerStoreOrderScheduleDayRequest request
    ) {
        if (!request.available()) {
            return StoreOrderScheduleDay.createUnavailable(dayOfWeek);
        }

        return StoreOrderScheduleDay.createAvailable(
                dayOfWeek,
                request.minOrderQuantity(),
                request.maxOrderQuantity(),
                toTimeRanges(request.pickupTimeRanges(), request.breakTimeRanges())
        );
    }

    private List<OwnerStoreOrderScheduleRequest.DayScheduleRequest> nonNullDays(
            List<OwnerStoreOrderScheduleRequest.DayScheduleRequest> days
    ) {
        return days != null ? days : List.of();
    }

    private List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> nonNullTimeRanges(
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> timeRanges
    ) {
        return timeRanges != null ? timeRanges : List.of();
    }

    private void validateTimeRanges(List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> timeRanges) {
        for (OwnerStoreOrderScheduleRequest.TimeRangeRequest timeRange : timeRanges) {
            if (timeRange == null || timeRange.startTime() == null || timeRange.endTime() == null) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            if (!timeRange.startTime().isBefore(timeRange.endTime())) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            if (!isThirtyMinuteUnit(timeRange.startTime()) || !isThirtyMinuteUnit(timeRange.endTime())) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
        }
    }

    private void validateNoOverlap(List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> timeRanges) {
        List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> sortedTimeRanges = timeRanges.stream()
                .sorted(Comparator.comparing(OwnerStoreOrderScheduleRequest.TimeRangeRequest::startTime))
                .toList();

        for (int i = 1; i < sortedTimeRanges.size(); i++) {
            OwnerStoreOrderScheduleRequest.TimeRangeRequest previous = sortedTimeRanges.get(i - 1);
            OwnerStoreOrderScheduleRequest.TimeRangeRequest current = sortedTimeRanges.get(i);
            if (current.startTime().isBefore(previous.endTime())) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
        }
    }

    private void validateBreakTimesWithinPickupTimes(
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> pickupTimeRanges,
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> breakTimeRanges
    ) {
        for (OwnerStoreOrderScheduleRequest.TimeRangeRequest breakTimeRange : breakTimeRanges) {
            boolean includedInPickupTime = pickupTimeRanges.stream()
                    .anyMatch(pickupTimeRange ->
                            !breakTimeRange.startTime().isBefore(pickupTimeRange.startTime())
                                    && !breakTimeRange.endTime().isAfter(pickupTimeRange.endTime())
                    );
            if (!includedInPickupTime) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
        }
    }

    private boolean isThirtyMinuteUnit(LocalTime time) {
        return time.getSecond() == 0
                && time.getNano() == 0
                && (time.getMinute() == 0 || time.getMinute() == 30);
    }

    private List<StoreOrderScheduleTimeRange> toTimeRanges(
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> pickupTimeRanges,
            List<OwnerStoreOrderScheduleRequest.TimeRangeRequest> breakTimeRanges
    ) {
        List<StoreOrderScheduleTimeRange> timeRanges = new ArrayList<>();

        int sortOrder = 0;
        for (OwnerStoreOrderScheduleRequest.TimeRangeRequest request : nonNullTimeRanges(pickupTimeRanges)) {
            timeRanges.add(StoreOrderScheduleTimeRange.pickup(request.startTime(), request.endTime(), sortOrder++));
        }

        sortOrder = 0;
        for (OwnerStoreOrderScheduleRequest.TimeRangeRequest request : nonNullTimeRanges(breakTimeRanges)) {
            timeRanges.add(StoreOrderScheduleTimeRange.breakTime(request.startTime(), request.endTime(), sortOrder++));
        }

        return timeRanges;
    }
}
