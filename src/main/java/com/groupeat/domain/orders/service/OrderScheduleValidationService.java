package com.groupeat.domain.orders.service;

import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.store.entity.StoreOrderSchedule;
import com.groupeat.domain.store.entity.StoreOrderScheduleDay;
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
        if (pickupTime.isBefore(daySchedule.getPickupOpenTime()) || pickupTime.isAfter(daySchedule.getPickupCloseTime())) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }

        long minutesFromOpen = ChronoUnit.MINUTES.between(daySchedule.getPickupOpenTime(), pickupTime);
        if (minutesFromOpen % daySchedule.getIntervalMinutes() != 0) {
            throw new GeneralException(OrderErrorStatus.ORDER_SCHEDULE_NOT_AVAILABLE);
        }
    }

    private void validateRequestedQuantity(StoreOrderScheduleDay daySchedule, int requestedQuantity) {
        if (requestedQuantity < daySchedule.getMinOrderQuantity()
                || requestedQuantity > daySchedule.getMaxOrderQuantity()) {
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

        if (acceptedQuantity + requestedQuantity > daySchedule.getMaxOrderQuantity()) {
            throw new GeneralException(OrderErrorStatus.ORDER_QUANTITY_NOT_AVAILABLE);
        }
    }
}
