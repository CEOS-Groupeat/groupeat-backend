package com.groupeat.domain.terms.dto;

import com.groupeat.domain.terms.entity.Terms;

public record TermsResponse(
        Long termsId,
        String title,
        String content,
        boolean required,
        String targetType,
        String version
) {
    public static TermsResponse from(Terms terms) {
        return new TermsResponse(
                terms.getId(),
                terms.getTitle(),
                terms.getContent(),
                terms.isRequired(),
                terms.getTargetType().name(),
                terms.getVersion()
        );
    }
}
