package com.groupeat.domain.terms.service;

import com.groupeat.domain.signup.dto.SignupAgreementRequest;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TermsAgreementValidator {

    private final TermsRepository termsRepository;

    public void validateRequiredTermsAgreed(
            TermsTargetType targetType,
            List<SignupAgreementRequest> agreements
    ) {
        List<Terms> activeTerms = termsRepository.findByTargetTypeAndActiveTrue(targetType);

        validateAgreementTarget(targetType, agreements, activeTerms);

        List<Terms> requiredTerms = activeTerms.stream()
                .filter(Terms::isRequired)
                .toList();

        if (requiredTerms.isEmpty()) {
            throw new IllegalStateException("활성화된 필수 약관이 존재하지 않습니다. targetType=" + targetType);
        }

        Map<Long, Boolean> agreementMap = agreements.stream()
                .collect(Collectors.toMap(
                        SignupAgreementRequest::termsId,
                        SignupAgreementRequest::agreed,
                        (oldValue, newValue) -> newValue
                ));

        for (Terms terms : requiredTerms) {
            Boolean agreed = agreementMap.get(terms.getId());

            if (!Boolean.TRUE.equals(agreed)) {
                throw new IllegalArgumentException("필수 약관에 동의하지 않았습니다. termsId=" + terms.getId());
            }
        }
    }

    private void validateAgreementTarget(
            TermsTargetType targetType,
            List<SignupAgreementRequest> agreements,
            List<Terms> activeTerms
    ) {
        Set<Long> activeTargetTermIds = activeTerms.stream()
                .map(Terms::getId)
                .collect(Collectors.toSet());

        boolean hasInvalidTerms = agreements.stream()
                .map(SignupAgreementRequest::termsId)
                .anyMatch(termsId -> !activeTargetTermIds.contains(termsId));

        if (hasInvalidTerms) {
            throw new IllegalArgumentException("약관 대상이 올바르지 않습니다. targetType=" + targetType);
        }
    }
}
