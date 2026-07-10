package com.groupeat.domain.store.service;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.store.converter.StoreOrderScheduleConverter;
import com.groupeat.domain.store.dto.request.OwnerStoreOrderScheduleRequest;
import com.groupeat.domain.store.dto.response.OwnerStoreOrderScheduleResponse;
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
import java.time.LocalTime;
import java.util.Arrays;
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
                    request.minimumOrderDeadlineDays(),
                    days
            );
            schedule = scheduleRepository.save(schedule);
        } else {
            schedule.updatePeriod(
                    request.startDate(),
                    request.endDate(),
                    request.minimumOrderDeadlineDays()
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
        if (request == null
                || request.startDate() == null
                || request.endDate() == null
                || request.minimumOrderDeadlineDays() == null
                || request.dailySchedules() == null
                || request.dailySchedules().size() != DayOfWeek.values().length) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        Map<DayOfWeek, OwnerStoreOrderScheduleRequest.DailyScheduleRequest> dailySchedulesByDay =
                new EnumMap<>(DayOfWeek.class);

        for (OwnerStoreOrderScheduleRequest.DailyScheduleRequest dailySchedule : request.dailySchedules()) {
            if (dailySchedule == null || dailySchedule.dayOfWeek() == null || dailySchedule.available() == null) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            if (dailySchedulesByDay.put(dailySchedule.dayOfWeek(), dailySchedule) != null) {
                throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
            }
            validateDailyScheduleRequest(dailySchedule);
        }

        if (!dailySchedulesByDay.keySet().containsAll(Arrays.asList(DayOfWeek.values()))) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }
    }

    private void validateDailyScheduleRequest(OwnerStoreOrderScheduleRequest.DailyScheduleRequest request) {
        if (!request.available()) {
            return;
        }

        if (request.minOrderQuantity() == null
                || request.maxOrderQuantity() == null
                || request.pickupTimeRange() == null) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        if (request.minOrderQuantity() > request.maxOrderQuantity()) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        validateTimeRange(request.pickupTimeRange());
        if (request.breakTimeRange() != null) {
            validateTimeRange(request.breakTimeRange());
            validateBreakTimeWithinPickupTime(request.pickupTimeRange(), request.breakTimeRange());
        }
    }

    private List<StoreOrderScheduleDay> toScheduleDays(OwnerStoreOrderScheduleRequest request) {
        return request.dailySchedules().stream()
                .map(this::toScheduleDay)
                .toList();
    }

    private StoreOrderScheduleDay toScheduleDay(OwnerStoreOrderScheduleRequest.DailyScheduleRequest request) {
        if (!request.available()) {
            return StoreOrderScheduleDay.createUnavailable(request.dayOfWeek());
        }

        return StoreOrderScheduleDay.createAvailable(
                request.dayOfWeek(),
                request.minOrderQuantity(),
                request.maxOrderQuantity(),
                request.pickupTimeRange().startTime(),
                request.pickupTimeRange().endTime(),
                request.breakTimeRange() != null ? request.breakTimeRange().startTime() : null,
                request.breakTimeRange() != null ? request.breakTimeRange().endTime() : null
        );
    }

    private void validateTimeRange(OwnerStoreOrderScheduleRequest.TimeRangeRequest timeRange) {
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

    private void validateBreakTimeWithinPickupTime(
            OwnerStoreOrderScheduleRequest.TimeRangeRequest pickupTimeRange,
            OwnerStoreOrderScheduleRequest.TimeRangeRequest breakTimeRange
    ) {
        boolean includedInPickupTime = !breakTimeRange.startTime().isBefore(pickupTimeRange.startTime())
                && !breakTimeRange.endTime().isAfter(pickupTimeRange.endTime());
        if (!includedInPickupTime) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }
    }

    private boolean isThirtyMinuteUnit(LocalTime time) {
        return time.getSecond() == 0
                && time.getNano() == 0
                && (time.getMinute() == 0 || time.getMinute() == 30);
    }

}
