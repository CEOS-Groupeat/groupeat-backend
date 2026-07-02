package com.groupeat.domain.terms.dto;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;

import java.time.LocalDateTime;

public record CustomerTermsDetailResponse(
        Long termsId,
        String title,
        String content,
        boolean required,
        String targetType,
        String version,
        boolean agreed,
        LocalDateTime agreedAt,
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
