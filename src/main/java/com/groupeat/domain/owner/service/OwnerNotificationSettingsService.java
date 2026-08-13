package com.groupeat.domain.owner.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.owner.dto.request.OwnerNotificationSettingsUpdateRequest;
import com.groupeat.domain.owner.dto.response.OwnerNotificationSettingsResponse;
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
public class OwnerNotificationSettingsService {

    private static final Set<TermsTargetType> OWNER_TARGET_TYPES =
            Set.of(TermsTargetType.COMMON, TermsTargetType.BUSINESS);

    private final OwnerMyPageService ownerMyPageService;
    private final MarketingTermsAgreementService marketingTermsAgreementService;

    // 사업자 알림설정 조회
    public OwnerNotificationSettingsResponse getSettings(Long memberId) {
        Member member = ownerMyPageService.getActiveBusinessOwner(memberId);
        Terms marketingTerms = marketingTermsAgreementService.getMarketingTerms(OWNER_TARGET_TYPES);
        MemberTermsAgreement marketingAgreement =
                marketingTermsAgreementService.getLatestAgreement(memberId, marketingTerms.getId());

        return OwnerNotificationSettingsResponse.of(member, marketingTerms, marketingAgreement);
    }

    // 사업자 알림설정 수정
    @Transactional
    public OwnerNotificationSettingsResponse updateSettings(
            Long memberId,
            OwnerNotificationSettingsUpdateRequest request
    ) {
        if (!request.hasAnySetting()) {
            throw new GeneralException(GlobalErrorStatus._BAD_REQUEST);
        }

        Member member = ownerMyPageService.getActiveBusinessOwner(memberId);
        Terms marketingTerms = marketingTermsAgreementService.getMarketingTerms(OWNER_TARGET_TYPES);
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

        return OwnerNotificationSettingsResponse.of(member, marketingTerms, marketingAgreement);
    }
}
