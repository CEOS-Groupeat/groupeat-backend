package com.groupeat.domain.signup.service;

import com.groupeat.domain.auth.jwt.SignupTokenPayload;
import com.groupeat.domain.auth.jwt.SignupTokenProvider;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.signup.dto.CommonSignupRequest;
import com.groupeat.domain.signup.dto.CommonSignupResponse;
import com.groupeat.domain.signup.dto.SignupAgreementRequest;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.repository.MemberTermsAgreementRepository;
import com.groupeat.domain.terms.service.TermsAgreementValidator;
import com.groupeat.domain.verification.phone.service.PhoneVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private final SignupTokenProvider signupTokenProvider;
    private final PhoneVerificationService phoneVerificationService;
    private final TermsAgreementValidator termsAgreementValidator;

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberTermsAgreementRepository memberTermsAgreementRepository;

    public CommonSignupResponse signupCommon(CommonSignupRequest request) {
        SignupTokenPayload payload = signupTokenProvider.getPayload(request.signupToken());

        validateNotRegisteredSocialAccount(payload);

        phoneVerificationService.validateVerifiedPhoneNumber(request.phoneNumber());

        termsAgreementValidator.validateRequiredTermsAgreed(
                TermsTargetType.COMMON,
                request.agreements()
        );

        Member member = Member.createInProgress(
                payload.memberType(),
                request.phoneNumber()
        );

        Member savedMember = memberRepository.save(member);

        SocialAccount socialAccount = SocialAccount.create(
                savedMember.getId(),
                payload.provider(),
                payload.providerUserId(),
                payload.email()
        );

        socialAccountRepository.save(socialAccount);

        saveTermsAgreements(savedMember.getId(), request.agreements());

        return new CommonSignupResponse(
                savedMember.getId(),
                savedMember.getMemberType(),
                getNextStep(savedMember)
        );
    }

    private void validateNotRegisteredSocialAccount(SignupTokenPayload payload) {
        boolean exists = socialAccountRepository
                .findByProviderAndProviderUserId(
                        payload.provider(),
                        payload.providerUserId()
                )
                .isPresent();

        if (exists) {
            throw new IllegalArgumentException("이미 가입된 소셜 계정입니다.");
        }
    }

    private void saveTermsAgreements(
            Long memberId,
            List<SignupAgreementRequest> agreements
    ) {
        List<MemberTermsAgreement> agreementEntities = agreements.stream()
                .map(agreement -> MemberTermsAgreement.create(
                        memberId,
                        agreement.termsId(),
                        agreement.agreed()
                ))
                .toList();

        memberTermsAgreementRepository.saveAll(agreementEntities);
    }

    private String getNextStep(Member member) {
        return switch (member.getMemberType()) {
            case CUSTOMER -> "CUSTOMER_PROFILE";
            case BUSINESS -> "BUSINESS_PROFILE";
        };
    }
}
