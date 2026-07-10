package com.groupeat.domain.store.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "store_order_schedule_day",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_store_order_schedule_day_schedule_day",
                        columnNames = {"store_order_schedule_id", "day_of_week"}
                )
        }
)
public class StoreOrderScheduleDay extends BaseEntity {

    public static final int DEFAULT_INTERVAL_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_order_schedule_day_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_schedule_id", nullable = false)
    private StoreOrderSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(name = "available", nullable = false)
    private boolean available;

    @Column(name = "min_order_quantity")
    private Integer minOrderQuantity;

    @Column(name = "max_order_quantity")
    private Integer maxOrderQuantity;

    @Column(name = "interval_minutes", nullable = false)
    private Integer intervalMinutes;

    @Column(name = "pickup_start_time")
    private LocalTime pickupStartTime;

    @Column(name = "pickup_end_time")
    private LocalTime pickupEndTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    public static StoreOrderScheduleDay createAvailable(
            DayOfWeek dayOfWeek,
            Integer minOrderQuantity,
            Integer maxOrderQuantity,
            LocalTime pickupStartTime,
            LocalTime pickupEndTime,
            LocalTime breakStartTime,
            LocalTime breakEndTime
    ) {
        return StoreOrderScheduleDay.builder()
                .dayOfWeek(dayOfWeek)
                .available(true)
                .minOrderQuantity(minOrderQuantity)
                .maxOrderQuantity(maxOrderQuantity)
                .intervalMinutes(DEFAULT_INTERVAL_MINUTES)
                .pickupStartTime(pickupStartTime)
                .pickupEndTime(pickupEndTime)
                .breakStartTime(breakStartTime)
                .breakEndTime(breakEndTime)
                .build();
    }

    public static StoreOrderScheduleDay createUnavailable(DayOfWeek dayOfWeek) {
        return StoreOrderScheduleDay.builder()
                .dayOfWeek(dayOfWeek)
                .available(false)
                .intervalMinutes(DEFAULT_INTERVAL_MINUTES)
                .build();
    }

    public void updateFrom(StoreOrderScheduleDay day) {
        this.available = day.available;
        this.minOrderQuantity = day.minOrderQuantity;
        this.maxOrderQuantity = day.maxOrderQuantity;
        this.intervalMinutes = day.intervalMinutes;
        this.pickupStartTime = day.pickupStartTime;
        this.pickupEndTime = day.pickupEndTime;
        this.breakStartTime = day.breakStartTime;
        this.breakEndTime = day.breakEndTime;
    }

    void assignSchedule(StoreOrderSchedule schedule) {
        this.schedule = schedule;
    }
}
