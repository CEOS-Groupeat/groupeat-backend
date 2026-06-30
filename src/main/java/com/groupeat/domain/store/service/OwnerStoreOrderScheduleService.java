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
import java.util.ArrayList;
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
            validateDayRequest(dayRequest);
        }
    }

    private void validateDayRequest(OwnerStoreOrderScheduleRequest.DayScheduleRequest dayRequest) {
        if (!dayRequest.available()) {
            return;
        }

        if (dayRequest.minOrderQuantity() == null
                || dayRequest.maxOrderQuantity() == null
                || dayRequest.pickupOpenTime() == null
                || dayRequest.pickupCloseTime() == null) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        if (dayRequest.minOrderQuantity() > dayRequest.maxOrderQuantity()) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }

        if (!dayRequest.pickupOpenTime().isBefore(dayRequest.pickupCloseTime())) {
            throw new GeneralException(StoreErrorStatus.INVALID_ORDER_SCHEDULE);
        }
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
                    dayRequest.pickupOpenTime(),
                    dayRequest.pickupCloseTime(),
                    dayRequest.intervalMinutes()
            ));
        }

        return days;
    }

    private List<OwnerStoreOrderScheduleRequest.DayScheduleRequest> nonNullDays(
            List<OwnerStoreOrderScheduleRequest.DayScheduleRequest> days
    ) {
        return days != null ? days : List.of();
    }
}
