package com.groupeat.domain.admin.business.dto.response;

import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

            @Schema(description = "상호명", example = "사르르 연남")
            String businessName,

            @Schema(description = "대표자명", example = "안세빈")
            String representativeName,

            @Schema(description = "현재 심사 상태", example = "PENDING")
            BusinessVerificationStatus status,

            @Schema(description = "인증 신청 날짜", example = "2026-06-27")
            LocalDate requestedDate,

            @Schema(description = "인증 신청 시간", example = "16:30:00")
            LocalTime requestedTime
    ) {}
}