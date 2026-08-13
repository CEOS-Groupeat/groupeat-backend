package com.groupeat.domain.admin.business.converter;

import com.groupeat.domain.admin.business.dto.response.AdminVerificationDetailResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationListResponse;
import com.groupeat.domain.admin.business.dto.response.AdminVerificationProcessResponse;
import com.groupeat.domain.business.entity.BusinessProfile;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.global.dto.CursorResponse;

import java.util.List;

public class AdminBusinessConverter {

    public static AdminVerificationProcessResponse toVerificationProcessResponse(BusinessProfile profile) {
        return AdminVerificationProcessResponse.builder()
                .businessProfileId(profile.getId())
                .status(profile.getVerificationStatus())
                .reviewedDate(profile.getReviewedAt() != null ? profile.getReviewedAt().toLocalDate() : null)
                .reviewedTime(profile.getReviewedAt() != null ? profile.getReviewedAt().toLocalTime() : null)
                .build();
    }

    public static AdminVerificationListResponse.VerificationListDTO toVerificationListDTO(
            CursorResponse<BusinessProfile> cursorResponse,
            long totalElements
    ) {
        CursorResponse<AdminVerificationListResponse.VerificationCardDTO> dtoCursorResponse =
                cursorResponse.map(AdminBusinessConverter::toVerificationCardDTO);

        return AdminVerificationListResponse.VerificationListDTO.builder()
                .totalElements(totalElements)
                .verificationList(dtoCursorResponse.content())
                .hasNext(dtoCursorResponse.hasNext())
                .nextCursor(dtoCursorResponse.nextCursor())
                .build();
    }

    private static AdminVerificationListResponse.VerificationCardDTO toVerificationCardDTO(BusinessProfile profile) {
        return AdminVerificationListResponse.VerificationCardDTO.builder()
                .businessProfileId(profile.getId())
                .businessName(profile.getBusinessName())
                .representativeName(profile.getRepresentativeName())
                .status(profile.getVerificationStatus())
                .requestedDate(profile.getCreatedAt().toLocalDate())
                .requestedTime(profile.getCreatedAt().toLocalTime())
                .build();
    }

    public static AdminVerificationDetailResponse toVerificationDetailResponse(BusinessProfile profile, Member member) {
        return AdminVerificationDetailResponse.builder()
                .businessProfileId(profile.getId())
                .status(profile.getVerificationStatus())

                // 회원 정보 매핑 (Member)
                .memberName(member.getName())
                .birthDate(member.getBirthDate())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .gender(member.getGender() != null ? member.getGender().name() : null)

                // 사업자 정보 매핑 (BusinessProfile)
                .representativeName(profile.getRepresentativeName())
                .businessName(profile.getBusinessName())
                .openedDate(profile.getOpenedDate())
                .businessType(profile.getBusinessType())
                .businessRegistrationNumber(profile.getBusinessRegistrationNumber())
                .businessRegistrationCertificateUrl(profile.getBusinessRegistrationCertificateUrl())
                .build();
    }
}

