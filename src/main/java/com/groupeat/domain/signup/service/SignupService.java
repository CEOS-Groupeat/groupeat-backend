package com.groupeat.domain.signup.service;

import com.groupeat.domain.auth.jwt.SignupTokenPayload;
import com.groupeat.domain.auth.jwt.SignupTokenProvider;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.signup.dto.*;
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

    // 공통 회원가입
    public CommonSignupResponse signupCommon(CommonSignupRequest request) {
        SignupTokenPayload payload = signupTokenProvider.getPayload(request.signupToken());

        validateNotRegisteredSocialAccount(payload);

        phoneVerificationService.validateVerifiedPhoneNumber(request.phoneNumber());
        validatePhoneNumberNotUsed(request.phoneNumber());

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

    // Customer 추가 회원가입
    public CustomerSignupResponse signupCustomer(CustomerSignupRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        validateCustomerSignupAvailable(member);

        validateCustomerUniqueFields(request);

        termsAgreementValidator.validateRequiredTermsAgreed(
                TermsTargetType.CUSTOMER,
                request.agreements()
        );

        saveTermsAgreements(member.getId(), request.agreements());

        member.completeCustomerSignup(
                request.name(),
                request.nickname(),
                request.email(),
                request.age(),
                request.gender()
        );

        return new CustomerSignupResponse(
                member.getId(),
                member.getMemberType(),
                member.getMemberStatus(),
                "고객 회원가입이 완료되었습니다."
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

    private void validatePhoneNumberNotUsed(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
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

    private void validateCustomerSignupAvailable(Member member) {
        if (member.getMemberType() != MemberType.CUSTOMER) {
            throw new IllegalArgumentException("고객 회원이 아닙니다.");
        }

        if (member.getMemberStatus() != MemberStatus.SIGNUP_IN_PROGRESS) {
            throw new IllegalArgumentException("회원가입을 진행할 수 없는 상태입니다.");
        }
    }

    private void validateCustomerUniqueFields(CustomerSignupRequest request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }
}
