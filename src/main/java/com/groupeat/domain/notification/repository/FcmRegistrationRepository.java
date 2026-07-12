package com.groupeat.domain.notification.repository;

import com.groupeat.domain.notification.entity.FcmRegistration;
import com.groupeat.domain.notification.enums.FcmPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmRegistrationRepository extends JpaRepository<FcmRegistration, Long> {

    Optional<FcmRegistration> findByRegistrationToken(String registrationToken);

    Optional<FcmRegistration> findByMemberIdAndRegistrationToken(Long memberId, String registrationToken);

    List<FcmRegistration> findAllByMemberIdAndPlatformAndActiveTrue(Long memberId, FcmPlatform platform);
}
