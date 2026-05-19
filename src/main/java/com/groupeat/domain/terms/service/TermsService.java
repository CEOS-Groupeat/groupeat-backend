package com.groupeat.domain.terms.service;

import com.groupeat.domain.terms.TermsResponse;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;

    public List<TermsResponse> getTerms(TermsTargetType targetType) {
        List<Terms> termsList = termsRepository.findByTargetTypeAndActiveTrue(targetType);

        return termsList.stream()
                .map(TermsResponse::from)
                .toList();
    }
}
