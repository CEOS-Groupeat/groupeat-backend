package com.groupeat.domain.business.entity;

import com.groupeat.domain.business.enums.BusinessType;
import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "business_profile",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_business_profile_member_id",
                        columnNames = "member_id"
                ),
                @UniqueConstraint(
                        name = "uk_business_profile_registration_number",
                        columnNames = "business_registration_number"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType businessType;

    @Column(nullable = false)
    private String representativeName;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private LocalDate openedDate;

    @Column(name = "business_registration_number", nullable = false)
    private String businessRegistrationNumber;

    @Column(nullable = false)
    private String businessRegistrationCertificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessVerificationStatus verificationStatus;

    private String rejectionReason;

    private LocalDateTime reviewedAt;

    private Long reviewerAdminId;

    public static BusinessProfile createPending(
            Long memberId,
            BusinessType businessType,
            String representativeName,
            String businessName,
            LocalDate openedDate,
            String businessRegistrationNumber,
            String businessRegistrationCertificateUrl
    ) {
        BusinessProfile businessProfile = new BusinessProfile();
        businessProfile.memberId = memberId;
        businessProfile.businessType = businessType;
        businessProfile.representativeName = representativeName;
        businessProfile.businessName = businessName;
        businessProfile.openedDate = openedDate;
        businessProfile.businessRegistrationNumber = businessRegistrationNumber;
        businessProfile.businessRegistrationCertificateUrl = businessRegistrationCertificateUrl;
        businessProfile.verificationStatus = BusinessVerificationStatus.PENDING;
        return businessProfile;
    }

    public void approve(Long reviewerAdminId) {
        this.verificationStatus = BusinessVerificationStatus.APPROVED;
        this.rejectionReason = null;
        this.reviewerAdminId = reviewerAdminId;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(Long reviewerAdminId, String rejectionReason) {
        this.verificationStatus = BusinessVerificationStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.reviewerAdminId = reviewerAdminId;
        this.reviewedAt = LocalDateTime.now();
    }
}
