package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
import com.groupeat.domain.store.entity.StoreOrderScheduleTimeRange;
import com.groupeat.domain.store.enums.StoreOrderScheduleTimeRangeType;
import com.groupeat.domain.store.repository.StoreOrderScheduleRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderScheduleValidationService {

    private final StoreOrderScheduleRepository storeOrderScheduleRepository;
    private final OrderRepository orderRepository;

    public void validateOrderCreation(
            Long storeId,
            LocalDate pickupDate,
            LocalTime pickupTime,
            int requestedQuantity
    ) {
        StoreOrderScheduleDay daySchedule = validateSchedule(storeId, pickupDate, pickupTime, requestedQuantity);
        validateAcceptedQuantityLimit(storeId, pickupDate, requestedQuantity, daySchedule);
    }

    public void validateOrderAcceptance(Order order) {
        int orderQuantity = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        StoreOrderScheduleDay daySchedule = validateSchedule(
                order.getStore().getId(),
                order.getPickupDate(),
                order.getPickupTime(),
                orderQuantity
        );
        validateAcceptedQuantityLimit(order.getStore().getId(), order.getPickupDate(), orderQuantity, daySchedule);
    }

    private StoreOrderScheduleDay validateSchedule(
            Long storeId,
            LocalDate pickupDate,
            LocalTime pickupTime,
            int requestedQuantity
    ) {
        StoreOrderSchedule schedule = storeOrderScheduleRepository
                .findActiveScheduleByStoreIdAndDate(storeId, pickupDate)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE));

        if (!isLeadTimeEnough(schedule, pickupDate)) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }

        StoreOrderScheduleDay daySchedule = findDaySchedule(schedule, pickupDate.getDayOfWeek());
        if (daySchedule == null || !daySchedule.isAvailable()) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }

        validatePickupTime(daySchedule, pickupTime);
        validateRequestedQuantity(daySchedule, requestedQuantity);

        return daySchedule;
    }

    private boolean isLeadTimeEnough(StoreOrderSchedule schedule, LocalDate pickupDate) {
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), pickupDate);
        return daysBetween >= schedule.getMinOrderDays();
    }

    private StoreOrderScheduleDay findDaySchedule(StoreOrderSchedule schedule, DayOfWeek dayOfWeek) {
        return schedule.getDays().stream()
                .filter(daySchedule -> daySchedule.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);
    }

    private void validatePickupTime(StoreOrderScheduleDay daySchedule, LocalTime pickupTime) {
        boolean withinPickupTime = daySchedule.getTimeRanges().stream()
                .filter(timeRange -> timeRange.getType() == StoreOrderScheduleTimeRangeType.PICKUP)
                .anyMatch(timeRange -> containsInclusive(timeRange, pickupTime));
        if (!withinPickupTime) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }

        boolean withinBreakTime = daySchedule.getTimeRanges().stream()
                .filter(timeRange -> timeRange.getType() == StoreOrderScheduleTimeRangeType.BREAK)
                .anyMatch(timeRange -> containsStartInclusiveEndExclusive(timeRange, pickupTime));
        if (withinBreakTime) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }

        if (pickupTime.getSecond() != 0
                || pickupTime.getNano() != 0
                || (pickupTime.getMinute() != 0 && pickupTime.getMinute() != 30)) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }
    }

    private boolean containsInclusive(StoreOrderScheduleTimeRange timeRange, LocalTime pickupTime) {
        return !pickupTime.isBefore(timeRange.getStartTime()) && !pickupTime.isAfter(timeRange.getEndTime());
    }

    private boolean containsStartInclusiveEndExclusive(StoreOrderScheduleTimeRange timeRange, LocalTime pickupTime) {
        return !pickupTime.isBefore(timeRange.getStartTime()) && pickupTime.isBefore(timeRange.getEndTime());
    }

    private void validateRequestedQuantity(StoreOrderScheduleDay daySchedule, int requestedQuantity) {
        // 최소 수량 미달 시 전용 에러
        if (daySchedule.getMinOrderQuantity() != null && requestedQuantity < daySchedule.getMinOrderQuantity()) {
            throw new GeneralException(OrderErrorStatus.ORDER_QUANTITY_SHORTAGE);
        }

        // 최대 수량 초과 시 전용 에러
        if (daySchedule.getMaxOrderQuantity() != null && requestedQuantity > daySchedule.getMaxOrderQuantity()) {
            throw new GeneralException(OrderErrorStatus.ORDER_QUANTITY_NOT_AVAILABLE);
        }
    }

    private void validateAcceptedQuantityLimit(
            Long storeId,
            LocalDate pickupDate,
            int requestedQuantity,
            StoreOrderScheduleDay daySchedule
    ) {
        Long acceptedQuantity = orderRepository.sumOrderItemQuantityByStoreIdAndPickupDateAndStatus(
                storeId,
                pickupDate,
                OrderStatus.ACCEPTED
        );

        // 주문이 하나도 없으면 0
        long totalAccepted = acceptedQuantity != null ? acceptedQuantity : 0L;

        // 누적 수량 + 요청 수량이 최대치를 넘어가면 에러
        if (daySchedule.getMaxOrderQuantity() != null && totalAccepted + requestedQuantity > daySchedule.getMaxOrderQuantity()) {
            throw new GeneralException(OrderErrorStatus.ORDER_QUANTITY_NOT_AVAILABLE);
        }
    }
}
