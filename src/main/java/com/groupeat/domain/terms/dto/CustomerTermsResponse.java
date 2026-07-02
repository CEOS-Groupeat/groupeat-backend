package com.groupeat.domain.terms.dto;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;

import java.time.LocalDateTime;

public record CustomerTermsResponse(
        Long termsId,
        String title,
        boolean required,
        String version,
        boolean agreed,
        LocalDateTime agreedAt,
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
