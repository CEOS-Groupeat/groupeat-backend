package com.groupeat.domain.verification.phone.repository;

import com.groupeat.domain.verification.phone.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneNumberOrderByIdDesc(String phoneNumber);
}
