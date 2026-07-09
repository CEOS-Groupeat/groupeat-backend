package com.groupeat.domain.member.service;

import com.groupeat.domain.member.dto.request.CustomerNotificationSettingsUpdateRequest;
import com.groupeat.domain.member.dto.response.CustomerNotificationSettingsResponse;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.exception.TermsErrorStatus;
import com.groupeat.domain.terms.repository.MemberTermsAgreementRepository;
import com.groupeat.domain.terms.repository.TermsRepository;
import com.groupeat.global.apiPayload.code.status.GlobalErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerNotificationSettingsService {

    private static final Set<TermsTargetType> CUSTOMER_TARGET_TYPES =
            Set.of(TermsTargetType.COMMON, TermsTargetType.CUSTOMER);
    private static final String MARKETING_TERMS_KEYWORD = "마케팅";

    private final CustomerMyPageService customerMyPageService;
    private final TermsRepository termsRepository;
    private final MemberTermsAgreementRepository agreementRepository;

    public CustomerNotificationSettingsResponse getSettings(Long memberId) {
        Member member = customerMyPageService.getActiveCustomer(memberId);
        Terms marketingTerms = getMarketingTerms();
        MemberTermsAgreement marketingAgreement = getMarketingAgreement(memberId, marketingTerms.getId());

        return CustomerNotificationSettingsResponse.of(member, marketingTerms, marketingAgreement);
    }

    @Transactional
    public CustomerNotificationSettingsResponse updateSettings(
            Long memberId,
            CustomerNotificationSettingsUpdateRequest request
    ) {
        if (!request.hasAnySetting()) {
            throw new GeneralException(GlobalErrorStatus._BAD_REQUEST);
        }

        Member member = customerMyPageService.getActiveCustomer(memberId);
        Terms marketingTerms = getMarketingTerms();
        MemberTermsAgreement marketingAgreement = getMarketingAgreement(memberId, marketingTerms.getId());

        if (request.marketingAgreed() != null) {
            marketingAgreement = updateMarketingAgreement(
                    memberId,
                    marketingTerms.getId(),
                    marketingAgreement,
                    request.marketingAgreed()
            );
        }
        if (request.orderStatusNotificationAgreed() != null) {
            member.updateOrderStatusNotificationAgreement(request.orderStatusNotificationAgreed());
        }

        return CustomerNotificationSettingsResponse.of(member, marketingTerms, marketingAgreement);
    }

    private MemberTermsAgreement updateMarketingAgreement(
            Long memberId,
            Long termsId,
            MemberTermsAgreement agreement,
            boolean agreed
    ) {
        if (agreement == null) {
            return agreementRepository.save(MemberTermsAgreement.create(memberId, termsId, agreed));
        }
        if (agreement.isAgreed() != agreed) {
            agreement.updateAgreement(agreed);
        }
        return agreement;
    }

    private MemberTermsAgreement getMarketingAgreement(Long memberId, Long termsId) {
        return agreementRepository
                .findFirstByMemberIdAndTermsIdOrderByIdDesc(memberId, termsId)
                .orElse(null);
    }

    private Terms getMarketingTerms() {
        return termsRepository.findByTargetTypeInAndActiveTrueAndRequiredFalse(CUSTOMER_TARGET_TYPES)
                .stream()
                .filter(terms -> terms.getTitle().contains(MARKETING_TERMS_KEYWORD))
                .findFirst()
                .orElseThrow(() -> new GeneralException(TermsErrorStatus.MARKETING_TERMS_NOT_CONFIGURED));
    }
}
