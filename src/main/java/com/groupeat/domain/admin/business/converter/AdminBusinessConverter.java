package com.groupeat.domain.admin.business.converter;

import com.groupeat.domain.admin.business.dto.response.AdminVerificationListResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationProcessResponse;
import com.groupeat.domain.business.entity.BusinessProfile;

import java.util.List;

public class AdminBusinessConverter {

    public static AdminVerificationProcessResponse toVerificationProcessResponse(BusinessProfile profile) {
        return AdminVerificationProcessResponse.builder()
                .businessProfileId(profile.getId())
                .status(profile.getVerificationStatus())
                .reviewedAt(profile.getReviewedAt())
                .build();
    }

    public static AdminVerificationListResponse.VerificationListDTO toVerificationListDTO(
            List<BusinessProfile> profiles,
            long totalElements,
            boolean hasNext,
            Long nextCursor
    ) {
        List<AdminVerificationListResponse.VerificationCardDTO> cardDTOs = profiles.stream()
                .map(AdminBusinessConverter::toVerificationCardDTO)
                .toList();

        return AdminVerificationListResponse.VerificationListDTO.builder()
                .totalElements(totalElements)
                .verificationList(cardDTOs)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private static AdminVerificationListResponse.VerificationCardDTO toVerificationCardDTO(BusinessProfile profile) {
        return AdminVerificationListResponse.VerificationCardDTO.builder()
                .businessProfileId(profile.getId())
                .businessType(profile.getBusinessType())
                .businessName(profile.getBusinessName())
                .representativeName(profile.getRepresentativeName())
                .businessRegistrationNumber(profile.getBusinessRegistrationNumber())
                .openedDate(profile.getOpenedDate())
                .businessRegistrationCertificateUrl(profile.getBusinessRegistrationCertificateUrl())
                .status(profile.getVerificationStatus())
                .createdDate(profile.getCreatedAt().toLocalDate())
                .createdTime(profile.getCreatedAt().toLocalTime())
                .build();
    }
}
