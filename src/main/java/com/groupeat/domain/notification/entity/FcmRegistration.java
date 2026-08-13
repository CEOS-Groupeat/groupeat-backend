package com.groupeat.domain.notification.entity;

import com.groupeat.domain.notification.enums.FcmPlatform;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "fcm_registration")
public class FcmRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_registration_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "registration_token", nullable = false, unique = true, length = 512)
    private String registrationToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private FcmPlatform platform;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_registered_at", nullable = false)
    private LocalDateTime lastRegisteredAt;

    public static FcmRegistration create(
            Long memberId,
            String registrationToken,
            FcmPlatform platform,
            LocalDateTime registeredAt
    ) {
        return FcmRegistration.builder()
                .memberId(memberId)
                .registrationToken(registrationToken)
                .platform(platform)
                .active(true)
                .lastRegisteredAt(registeredAt)
                .build();
    }

    public void reactivate(Long memberId, FcmPlatform platform, LocalDateTime registeredAt) {
        this.memberId = memberId;
        this.platform = platform;
        this.active = true;
        this.lastRegisteredAt = registeredAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
