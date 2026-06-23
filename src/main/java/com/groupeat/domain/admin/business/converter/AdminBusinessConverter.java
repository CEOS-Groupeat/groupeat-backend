package com.groupeat.domain.admin.business.converter;

import com.groupeat.domain.admin.business.dto.response.AdminVerificationProcessResponse;
import com.groupeat.domain.business.entity.BusinessProfile;

public class AdminBusinessConverter {

    public static AdminVerificationProcessResponse toVerificationProcessResponse(BusinessProfile profile) {
        return AdminVerificationProcessResponse.builder()
                .businessProfileId(profile.getId())
                .status(profile.getVerificationStatus())
                .reviewedAt(profile.getReviewedAt())
                .build();
    }
}
