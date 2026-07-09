package com.groupeat.domain.owner.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.owner.dto.request.OwnerNotificationSettingsUpdateRequest;
import com.groupeat.domain.owner.dto.response.OwnerNotificationSettingsResponse;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.entity.Terms;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.enums.TermsType;
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
public class OwnerNotificationSettingsService {

    private static final Set<TermsTargetType> OWNER_TARGET_TYPES =
            Set.of(TermsTargetType.COMMON, TermsTargetType.BUSINESS);
    private static final String MARKETING_TERMS_KEYWORD = "마케팅";

    private final OwnerMyPageService ownerMyPageService;
    private final TermsRepository termsRepository;
    private final MemberTermsAgreementRepository agreementRepository;

    // 사업자 알림설정 조회
    public OwnerNotificationSettingsResponse getSettings(Long memberId) {
        Member member = ownerMyPageService.getActiveBusinessOwner(memberId);
        Terms marketingTerms = getMarketingTerms();
        MemberTermsAgreement marketingAgreement = getMarketingAgreement(memberId, marketingTerms.getId());

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

        return OwnerNotificationSettingsResponse.of(member, marketingTerms, marketingAgreement);
    }

    private MemberTermsAgreement updateMarketingAgreement(
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

    private MemberTermsAgreement getMarketingAgreement(Long memberId, Long termsId) {
        return agreementRepository
                .findFirstByMemberIdAndTermsIdOrderByIdDesc(memberId, termsId)
                .orElse(null);
    }

    private Terms getMarketingTerms() {
        return termsRepository.findFirstByTargetTypeInAndActiveTrueAndRequiredFalseAndType(
                        OWNER_TARGET_TYPES,
                        TermsType.MARKETING
                )
                .or(() -> termsRepository.findByTargetTypeInAndActiveTrueAndRequiredFalse(OWNER_TARGET_TYPES)
                        .stream()
                        .filter(terms -> terms.getTitle().contains(MARKETING_TERMS_KEYWORD))
                        .findFirst())
                .orElseThrow(() -> new GeneralException(TermsErrorStatus.MARKETING_TERMS_NOT_CONFIGURED));
    }
}
