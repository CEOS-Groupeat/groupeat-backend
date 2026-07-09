package com.groupeat.domain.store.entity;

import com.groupeat.domain.store.enums.StoreOrderScheduleTimeRangeType;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "store_order_schedule_time_range")
public class StoreOrderScheduleTimeRange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_order_schedule_time_range_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_schedule_day_id", nullable = false)
    private StoreOrderScheduleDay scheduleDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "range_type", nullable = false, length = 20)
    private StoreOrderScheduleTimeRangeType type;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static StoreOrderScheduleTimeRange pickup(LocalTime startTime, LocalTime endTime, int sortOrder) {
        return create(StoreOrderScheduleTimeRangeType.PICKUP, startTime, endTime, sortOrder);
    }

    public static StoreOrderScheduleTimeRange breakTime(LocalTime startTime, LocalTime endTime, int sortOrder) {
        return create(StoreOrderScheduleTimeRangeType.BREAK, startTime, endTime, sortOrder);
    }

    private static StoreOrderScheduleTimeRange create(
            StoreOrderScheduleTimeRangeType type,
            LocalTime startTime,
            LocalTime endTime,
            int sortOrder
    ) {
        return StoreOrderScheduleTimeRange.builder()
                .type(type)
                .startTime(startTime)
                .endTime(endTime)
                .sortOrder(sortOrder)
                .build();
    }

    void assignScheduleDay(StoreOrderScheduleDay scheduleDay) {
        this.scheduleDay = scheduleDay;
    }
}
