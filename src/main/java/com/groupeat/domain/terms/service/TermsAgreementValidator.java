package com.groupeat.domain.terms.service;

import com.groupeat.domain.signup.dto.SignupAgreementRequest;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TermsAgreementValidator {

    private final TermsRepository termsRepository;

    public void validateRequiredTermsAgreed(
            TermsTargetType targetType,
            List<SignupAgreementRequest> agreements
    ) {
        List<Terms> requiredTerms = termsRepository.findByTargetTypeAndActiveTrue(targetType)
                .stream()
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
}
