package com.groupeat.domain.terms.service;

import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.enums.TermsType;
import com.groupeat.domain.terms.exception.TermsErrorStatus;
import com.groupeat.domain.terms.repository.MemberTermsAgreementRepository;
import com.groupeat.domain.terms.repository.TermsRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketingTermsAgreementService {

    private final TermsRepository termsRepository;
    private final MemberTermsAgreementRepository agreementRepository;

    public Terms getMarketingTerms(Set<TermsTargetType> targetTypes) {
        return termsRepository.findFirstByTargetTypeInAndActiveTrueAndRequiredFalseAndType(
                        targetTypes,
                        TermsType.MARKETING
                )
                .orElseThrow(() -> new GeneralException(TermsErrorStatus.MARKETING_TERMS_NOT_CONFIGURED));
    }

    public MemberTermsAgreement getLatestAgreement(Long memberId, Long termsId) {
        return agreementRepository
                .findFirstByMemberIdAndTermsIdOrderByIdDesc(memberId, termsId)
                .orElse(null);
    }

    @Transactional
    public MemberTermsAgreement updateAgreement(
            Long memberId,
            Long termsId,
            MemberTermsAgreement agreement,
            boolean agreed
    ) {
        if (agreement == null || agreement.isAgreed() != agreed) {
            return agreementRepository.save(MemberTermsAgreement.create(memberId, termsId, agreed));
        }
        return agreement;
    }
}
