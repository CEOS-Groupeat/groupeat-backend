package com.groupeat.domain.admin.business.dto.response;

import com.groupeat.domain.business.enums.BusinessType;
import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AdminVerificationListResponse {

    @Builder
    public record VerificationListDTO(
            @Schema(description = "조건에 해당하는 전체 요청 개수 (옵션)", example = "15")
            long totalElements,

            @Schema(description = "인증 요청 카드 리스트")
            List<VerificationCardDTO> verificationList,

            @Schema(description = "다음 페이지 존재 여부 (무한 스크롤용)", example = "true")
            boolean hasNext,

            @Schema(description = "다음 커서 ID (마지막 프로필의 PK ID)", example = "15")
            Long nextCursor
    ) {}

    @Builder
    public record VerificationCardDTO(
            @Schema(description = "사업자 프로필 ID (PK)", example = "15")
            Long businessProfileId,

            @Schema(description = "사업자 유형 (개인/법인 등)", example = "CORPORATE")
            BusinessType businessType,

            @Schema(description = "상호명", example = "주식회사 맛있는그루핏")
            String businessName,

            @Schema(description = "대표자명", example = "홍길동")
            String representativeName,

            @Schema(description = "사업자등록번호 (10자리)", example = "1234567890")
            String businessRegistrationNumber,

            @Schema(description = "개업연월일", example = "2023-01-15")
            LocalDate openedDate,

            @Schema(description = "사업자등록증 원본 이미지 URL")
            String businessRegistrationCertificateUrl,

            @Schema(description = "현재 심사 상태", example = "PENDING")
            BusinessVerificationStatus status,

            @Schema(description = "인증 신청 일자", example = "2026-06-23")
            LocalDate createdDate,

            @Schema(description = "인증 신청 시간", example = "14:30:00")
            LocalTime createdTime
    ) {}
}