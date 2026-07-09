package com.groupeat.domain.member.dto.response;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CustomerNotificationSettingsResponse(
        @Schema(description = "마케팅 정보 수신 동의 약관 ID", example = "3")
        Long marketingTermsId,

        @Schema(description = "마케팅 정보 수신 동의 약관 제목", example = "마케팅 정보 수신 동의")
        String marketingTermsTitle,

        @Schema(description = "마케팅 정보 수신 동의 여부", example = "true")
        boolean marketingAgreed,

        @Schema(description = "마케팅 정보 수신 동의 시각. 미동의 상태이면 null", example = "2026-07-03T14:30:00", nullable = true)
        LocalDateTime marketingAgreedAt,

        @Schema(description = "주문 현황 알림 동의 여부", example = "true")
        boolean orderStatusNotificationAgreed
) {
    public static CustomerNotificationSettingsResponse of(
            Member member,
            Terms marketingTerms,
            MemberTermsAgreement marketingAgreement
    ) {
        return new CustomerNotificationSettingsResponse(
                marketingTerms.getId(),
                marketingTerms.getTitle(),
                marketingAgreement != null && marketingAgreement.isAgreed(),
                marketingAgreement == null ? null : marketingAgreement.getAgreedAt(),
                member.isOrderStatusNotificationAgreed()
        );
    }
}
