package com.groupeat.domain.admin.business.dto.response;

import com.groupeat.domain.business.enums.BusinessType;
import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AdminVerificationDetailResponse(
        @Schema(description = "사업자 프로필 ID", example = "15")
        Long businessProfileId,

        @Schema(description = "심사 상태", example = "PENDING")
        BusinessVerificationStatus status,

        // 회원 정보(Member)
        @Schema(description = "실명", example = "안세빈")
        String memberName,

        @Schema(description = "생년월일", example = "2003-09-30")
        LocalDate birthDate,

        @Schema(description = "이메일", example = "ansebin0930@gmail.com")
        String email,

        @Schema(description = "휴대폰 번호", example = "010-2653-7513")
        String phoneNumber,

        @Schema(description = "성별", example = "여성")
        String gender,

        // 사업자 정보(BusinessProfile)
        @Schema(description = "대표자명", example = "안세빈")
        String representativeName,

        @Schema(description = "상호명", example = "사르르 연남")
        String businessName,

        @Schema(description = "개업연월일", example = "2026-04-01")
        LocalDate openedDate,

        @Schema(description = "사업자 유형", example = "PERSONAL")
        BusinessType businessType,

        @Schema(description = "사업자등록번호", example = "000000-000000")
        String businessRegistrationNumber,

        @Schema(description = "사업자등록증 원본 이미지 URL")
        String businessRegistrationCertificateUrl
) {}
