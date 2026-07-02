package com.groupeat.domain.terms.service;

import com.groupeat.domain.member.service.CustomerMyPageService;
import com.groupeat.domain.terms.dto.CustomerTermsDetailResponse;
import com.groupeat.domain.terms.dto.CustomerTermsResponse;
import com.groupeat.domain.terms.dto.TermsAgreementUpdateRequest;
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
public class CustomerMyPageTermsService {

    private static final Set<TermsTargetType> CUSTOMER_TARGET_TYPES =
            Set.of(TermsTargetType.COMMON, TermsTargetType.CUSTOMER);

    private final TermsRepository termsRepository;
    private final MemberTermsAgreementRepository agreementRepository;
    private final CustomerMyPageService customerMyPageService;

    public List<CustomerTermsResponse> getTerms(Long memberId) {
        customerMyPageService.getActiveCustomer(memberId);

        List<Terms> termsList = termsRepository.findByTargetTypeInAndActiveTrue(CUSTOMER_TARGET_TYPES);
        List<Long> termsIds = termsList.stream().map(Terms::getId).toList();

        // 중복 이력이 존재하는 경우를 고려한 약관별 최신 동의 이력 구성
        Map<Long, MemberTermsAgreement> agreementByTermsId = agreementRepository
                .findByMemberIdAndTermsIdIn(memberId, termsIds)
                .stream()
                .collect(Collectors.toMap(
                        MemberTermsAgreement::getTermsId,
                        Function.identity(),
                        (previous, latest) -> previous.getId() > latest.getId() ? previous : latest
                ));

        return termsList.stream()
                .map(terms -> CustomerTermsResponse.of(terms, agreementByTermsId.get(terms.getId())))
                .toList();
    }

    public CustomerTermsDetailResponse getTermsDetail(Long memberId, Long termsId) {
        customerMyPageService.getActiveCustomer(memberId);
        Terms terms = getAccessibleTerms(termsId);
        MemberTermsAgreement agreement = agreementRepository
                .findFirstByMemberIdAndTermsIdOrderByIdDesc(memberId, termsId)
                .orElse(null);
        return CustomerTermsDetailResponse.of(terms, agreement);
    }

    @Transactional
    public CustomerTermsResponse updateTermsAgreement(
            Long memberId,
            Long termsId,
            TermsAgreementUpdateRequest request
    ) {
        customerMyPageService.getActiveCustomer(memberId);
        Terms terms = getAccessibleTerms(termsId);

        if (terms.isRequired()) {
            throw new GeneralException(TermsErrorStatus.REQUIRED_TERMS_NOT_MODIFIABLE);
        }

        // 기존 이력이 없는 선택 약관의 최초 동의 이력 생성
        MemberTermsAgreement agreement = agreementRepository
                .findFirstByMemberIdAndTermsIdOrderByIdDesc(memberId, termsId)
                .orElseGet(() -> agreementRepository.save(
                        MemberTermsAgreement.create(memberId, termsId, request.agreed())
                ));

        if (agreement.isAgreed() != request.agreed()) {
            agreement.updateAgreement(request.agreed());
        }
        return CustomerTermsResponse.of(terms, agreement);
    }

    private Terms getAccessibleTerms(Long termsId) {
        Terms terms = termsRepository.findByIdAndActiveTrue(termsId)
                .orElseThrow(() -> new GeneralException(TermsErrorStatus.TERMS_NOT_FOUND));
        if (!CUSTOMER_TARGET_TYPES.contains(terms.getTargetType())) {
            throw new GeneralException(TermsErrorStatus.TERMS_NOT_ACCESSIBLE);
        }
        return terms;
    }
}
