package com.groupeat.domain.business.repository;

import com.groupeat.domain.business.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
}
