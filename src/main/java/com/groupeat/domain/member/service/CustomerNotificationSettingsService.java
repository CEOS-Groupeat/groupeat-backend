package com.groupeat.domain.member.service;

import com.groupeat.domain.member.dto.request.CustomerNotificationSettingsUpdateRequest;
import com.groupeat.domain.member.dto.response.CustomerNotificationSettingsResponse;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.service.MarketingTermsAgreementService;
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

    private final CustomerMyPageService customerMyPageService;
    private final MarketingTermsAgreementService marketingTermsAgreementService;

    public CustomerNotificationSettingsResponse getSettings(Long memberId) {
        Member member = customerMyPageService.getActiveCustomer(memberId);
        Terms marketingTerms = marketingTermsAgreementService.getMarketingTerms(CUSTOMER_TARGET_TYPES);
        MemberTermsAgreement marketingAgreement =
                marketingTermsAgreementService.getLatestAgreement(memberId, marketingTerms.getId());

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
        Terms marketingTerms = marketingTermsAgreementService.getMarketingTerms(CUSTOMER_TARGET_TYPES);
        MemberTermsAgreement marketingAgreement =
                marketingTermsAgreementService.getLatestAgreement(memberId, marketingTerms.getId());

        if (request.marketingAgreed() != null) {
            marketingAgreement = marketingTermsAgreementService.updateAgreement(
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
}
