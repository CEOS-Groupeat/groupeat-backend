package com.groupeat.domain.terms.dto;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CustomerTermsResponse(
        @Schema(description = "약관 ID", example = "3")
        Long termsId,

        @Schema(description = "약관 제목", example = "마케팅 정보 수신 동의")
        String title,

        @Schema(description = "필수 약관 여부", example = "false")
        boolean required,

        @Schema(description = "약관 버전", example = "1.0")
        String version,

        @Schema(description = "현재 회원의 동의 여부", example = "true")
        boolean agreed,

        @Schema(description = "최근 동의 시각. 미동의 상태이면 null", example = "2026-07-03T14:30:00", nullable = true)
        LocalDateTime agreedAt,

        @Schema(description = "마이페이지에서 동의 상태 변경 가능 여부. 선택 약관만 true", example = "true")
        boolean modifiable
) {
    public static CustomerTermsResponse of(Terms terms, MemberTermsAgreement agreement) {
        return new CustomerTermsResponse(
                terms.getId(), terms.getTitle(), terms.isRequired(), terms.getVersion(),
                agreement != null && agreement.isAgreed(),
                agreement == null ? null : agreement.getAgreedAt(),
                !terms.isRequired()
        );
    }
}
