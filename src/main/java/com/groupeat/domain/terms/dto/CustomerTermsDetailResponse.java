package com.groupeat.domain.terms.dto;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CustomerTermsDetailResponse(
        @Schema(description = "약관 ID", example = "1")
        Long termsId,

        @Schema(description = "약관 제목", example = "서비스 이용약관")
        String title,

        @Schema(description = "약관 전문", example = "제1조 (목적) 본 약관은...")
        String content,

        @Schema(description = "필수 약관 여부", example = "true")
        boolean required,

        @Schema(description = "약관 대상. COMMON 또는 CUSTOMER", example = "COMMON")
        String targetType,

        @Schema(description = "약관 버전", example = "1.0")
        String version,

        @Schema(description = "현재 회원의 동의 여부", example = "true")
        boolean agreed,

        @Schema(description = "최근 동의 시각. 미동의 상태이면 null", example = "2026-07-03T14:30:00", nullable = true)
        LocalDateTime agreedAt,

        @Schema(description = "마이페이지에서 동의 상태 변경 가능 여부", example = "false")
        boolean modifiable
) {
    public static CustomerTermsDetailResponse of(Terms terms, MemberTermsAgreement agreement) {
        return new CustomerTermsDetailResponse(
                terms.getId(), terms.getTitle(), terms.getContent(), terms.isRequired(),
                terms.getTargetType().name(), terms.getVersion(),
                agreement != null && agreement.isAgreed(),
                agreement == null ? null : agreement.getAgreedAt(),
                !terms.isRequired()
        );
    }
}
