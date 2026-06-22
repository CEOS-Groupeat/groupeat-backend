package com.groupeat.domain.store.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "store_order_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_store_order_schedule_store_period",
                        columnNames = {"store_id", "start_date", "end_date"}
                )
        }
)
public class StoreOrderSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_order_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "min_order_days", nullable = false)
    private Integer minOrderDays; // 최소 주문 가능 기한

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StoreOrderScheduleDay> days = new ArrayList<>();

    public static StoreOrderSchedule create(
            Store store,
            LocalDate startDate,
            LocalDate endDate,
            Integer minOrderDays,
            List<StoreOrderScheduleDay> days
    ) {
        StoreOrderSchedule schedule = StoreOrderSchedule.builder()
                .store(store)
                .startDate(startDate)
                .endDate(endDate)
                .minOrderDays(minOrderDays)
                .build();
        schedule.replaceDays(days);
        return schedule;
    }

    public void updatePeriod(
            LocalDate startDate,
            LocalDate endDate,
            Integer minOrderDays
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.minOrderDays = minOrderDays;
    }

    public void replaceDays(List<StoreOrderScheduleDay> days) {
        this.days.clear();
        days.forEach(this::addDay);
    }

    public void addDay(StoreOrderScheduleDay day) {
        day.assignSchedule(this);
        this.days.add(day);
    }
}
