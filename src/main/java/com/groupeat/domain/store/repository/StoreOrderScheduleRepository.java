package com.groupeat.domain.store.repository;

import com.groupeat.domain.store.entity.StoreOrderSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StoreOrderScheduleRepository extends JpaRepository<StoreOrderSchedule, Long> {

    @EntityGraph(attributePaths = "days")
    @Query("""
            SELECT s
            FROM StoreOrderSchedule s
            WHERE s.store.id = :storeId
              AND s.startDate <= :date
              AND s.endDate >= :date
              AND s.deletedAt IS NULL
            """)
    Optional<StoreOrderSchedule> findActiveScheduleByStoreIdAndDate(
            @Param("storeId") Long storeId,
            @Param("date") LocalDate date
    );

    @EntityGraph(attributePaths = "days")
    Optional<StoreOrderSchedule> findFirstByStoreOwnerIdAndDeletedAtIsNullOrderByStartDateDesc(Long businessMemberId);

    boolean existsByStoreIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long storeId,
            LocalDate endDate,
            LocalDate startDate
    );
}
