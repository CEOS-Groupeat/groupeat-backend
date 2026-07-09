package com.groupeat.domain.owner.service;

import com.groupeat.domain.terms.dto.CustomerTermsDetailResponse;
import com.groupeat.domain.terms.dto.CustomerTermsResponse;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.exception.TermsErrorStatus;
import com.groupeat.domain.terms.repository.MemberTermsAgreementRepository;
import com.groupeat.domain.terms.repository.TermsRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerMyPageTermsService {

    private static final Set<TermsTargetType> OWNER_TARGET_TYPES =
            Set.of(TermsTargetType.COMMON, TermsTargetType.BUSINESS);

    private final TermsRepository termsRepository;
    private final MemberTermsAgreementRepository agreementRepository;
    private final OwnerMyPageService ownerMyPageService;

    public List<CustomerTermsResponse> getTerms(Long memberId) {
        ownerMyPageService.getActiveBusinessOwner(memberId);

        List<Terms> termsList = termsRepository.findByTargetTypeInAndActiveTrue(OWNER_TARGET_TYPES);
        List<Long> termsIds = termsList.stream().map(Terms::getId).toList();

        Map<Long, MemberTermsAgreement> agreementByTermsId = agreementRepository
                .findLatestByMemberIdAndTermsIdIn(memberId, termsIds)
                .stream()
                .collect(Collectors.toMap(
                        MemberTermsAgreement::getTermsId,
                        Function.identity()
                ));

        return termsList.stream()
                .filter(Terms::isRequired)
                .map(terms -> CustomerTermsResponse.of(terms, agreementByTermsId.get(terms.getId())))
                .toList();
    }

    public CustomerTermsDetailResponse getTermsDetail(Long memberId, Long termsId) {
        ownerMyPageService.getActiveBusinessOwner(memberId);
        Terms terms = getAccessibleTerms(termsId);
        if (!terms.isRequired()) {
            throw new GeneralException(TermsErrorStatus.TERMS_NOT_ACCESSIBLE);
        }
        MemberTermsAgreement agreement = agreementRepository
                .findFirstByMemberIdAndTermsIdOrderByIdDesc(memberId, termsId)
                .orElse(null);
        return CustomerTermsDetailResponse.of(terms, agreement);
    }

    private Terms getAccessibleTerms(Long termsId) {
        Terms terms = termsRepository.findByIdAndActiveTrue(termsId)
                .orElseThrow(() -> new GeneralException(TermsErrorStatus.TERMS_NOT_FOUND));
        if (!OWNER_TARGET_TYPES.contains(terms.getTargetType())) {
            throw new GeneralException(TermsErrorStatus.TERMS_NOT_ACCESSIBLE);
        }
        return terms;
    }
}
