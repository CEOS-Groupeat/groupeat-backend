package com.groupeat.domain.business.repository;

import com.groupeat.domain.business.entity.BusinessProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bp FROM BusinessProfile bp WHERE bp.id = :id")
    Optional<BusinessProfile> findByIdWithPessimisticLock(@Param("id") Long id);
}
