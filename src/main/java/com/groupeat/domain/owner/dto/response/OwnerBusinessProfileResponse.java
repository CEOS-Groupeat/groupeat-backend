package com.groupeat.domain.owner.dto.response;

import com.groupeat.domain.business.entity.BusinessProfile;
import com.groupeat.domain.business.enums.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record OwnerBusinessProfileResponse(
        @Schema(description = "대표자명", example = "홍길동")
        String representativeName,

        @Schema(description = "상호명", example = "그룹잇 케이터링")
        String businessName,

        @Schema(description = "개업연월일", example = "2020-03-15")
        LocalDate openedDate,

        @Schema(description = "사업자 유형", example = "INDIVIDUAL")
        BusinessType businessType,

        @Schema(description = "사업자등록번호", example = "1234567890")
        String businessRegistrationNumber,

        @Schema(description = "사업자등록증 파일 URL")
        String businessRegistrationCertificateUrl
) {
    public static OwnerBusinessProfileResponse from(BusinessProfile businessProfile) {
        return new OwnerBusinessProfileResponse(
                businessProfile.getRepresentativeName(),
                businessProfile.getBusinessName(),
                businessProfile.getOpenedDate(),
                businessProfile.getBusinessType(),
                businessProfile.getBusinessRegistrationNumber(),
                businessProfile.getBusinessRegistrationCertificateUrl()
        );
    }
}
