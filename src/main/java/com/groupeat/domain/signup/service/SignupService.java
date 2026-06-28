package com.groupeat.domain.signup.service;

import com.groupeat.domain.business.entity.BusinessProfile;
import com.groupeat.domain.business.jwt.BusinessValidationTokenPayload;
import com.groupeat.domain.business.jwt.BusinessValidationTokenProvider;
import com.groupeat.domain.business.repository.BusinessProfileRepository;
import com.groupeat.domain.auth.jwt.SignupTokenPayload;
import com.groupeat.domain.auth.jwt.SignupTokenProvider;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.signup.dto.*;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.domain.terms.entity.MemberTermsAgreement;
import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.repository.MemberTermsAgreementRepository;
import com.groupeat.domain.terms.service.TermsAgreementValidator;
import com.groupeat.domain.verification.phone.service.PhoneVerificationService;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private final SignupTokenProvider signupTokenProvider;
    private final PhoneVerificationService phoneVerificationService;
    private final TermsAgreementValidator termsAgreementValidator;
    private final BusinessValidationTokenProvider businessValidationTokenProvider;

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberTermsAgreementRepository memberTermsAgreementRepository;
    private final BusinessProfileRepository businessProfileRepository;


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
                request.memberType(),
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
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));
        String email = normalizeOptionalText(request.email());

        validateCustomerSignupAvailable(member);

        validateCustomerUniqueFields(email);

        termsAgreementValidator.validateRequiredTermsAgreed(
                TermsTargetType.CUSTOMER,
                request.agreements()
        );

        saveTermsAgreements(member.getId(), request.agreements());

        member.completeCustomerSignup(
                request.name(),
                email,
                request.birthDate(),
                request.gender()
        );

        return new CustomerSignupResponse(
                member.getId(),
                member.getMemberType(),
                member.getMemberStatus(),
                "고객 회원가입이 완료되었습니다."
        );
    }

    // Business 추가 회원가입
    public BusinessSignupResponse signupBusiness(BusinessSignupRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        validateBusinessSignupAvailable(member);

        // 프론트엔드가 보낸 토큰을 파싱하여 사업자번호 추출
        BusinessValidationTokenPayload payload = businessValidationTokenProvider.getPayload(request.businessValidationToken());
        String validBusinessNumber = payload.businessRegistrationNumber();

        validateBusinessUniqueFields(request, validBusinessNumber);

        termsAgreementValidator.validateRequiredTermsAgreed(
                TermsTargetType.BUSINESS,
                request.agreements()
        );

        saveTermsAgreements(member.getId(), request.agreements());

        member.completeBusinessBasicInfo(
                request.representativeName(),
                request.email(),
                request.birthDate(),
                request.gender()
        );

        BusinessProfile businessProfile = BusinessProfile.createPending(
                member.getId(),
                request.businessType(),
                request.representativeName(),
                request.businessName(),
                request.openedDate(),
                validBusinessNumber,
                request.businessRegistrationCertificateUrl()
        );

        BusinessProfile savedBusinessProfile = businessProfileRepository.save(businessProfile);

        return new BusinessSignupResponse(
                member.getId(),
                member.getMemberType(),
                member.getMemberStatus(),
                savedBusinessProfile.getVerificationStatus(),
                "사업자 회원가입 신청이 완료되었습니다."
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
            throw new GeneralException(SignupErrorStatus.SOCIAL_ACCOUNT_ALREADY_REGISTERED);
        }
    }

    private void validatePhoneNumberNotUsed(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new GeneralException(SignupErrorStatus.PHONE_NUMBER_ALREADY_REGISTERED);
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
            throw new GeneralException(SignupErrorStatus.NOT_CUSTOMER_MEMBER);
        }

        if (member.getMemberStatus() != MemberStatus.SIGNUP_IN_PROGRESS) {
            throw new GeneralException(SignupErrorStatus.SIGNUP_NOT_AVAILABLE);
        }
    }

    private void validateCustomerUniqueFields(String email) {
        if (email != null && memberRepository.existsByEmail(email)) {
            throw new GeneralException(SignupErrorStatus.EMAIL_ALREADY_EXISTS);
        }
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void validateBusinessSignupAvailable(Member member) {
        if (member.getMemberType() != MemberType.BUSINESS) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
        }

        if (member.getMemberStatus() != MemberStatus.SIGNUP_IN_PROGRESS) {
            throw new GeneralException(SignupErrorStatus.SIGNUP_NOT_AVAILABLE);
        }
    }

    private void validateBusinessUniqueFields(BusinessSignupRequest request, String validBusinessNumber) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new GeneralException(SignupErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        if (businessProfileRepository.existsByMemberId(request.memberId())) {
            throw new GeneralException(SignupErrorStatus.BUSINESS_PROFILE_ALREADY_EXISTS);
        }
        
        if (businessProfileRepository.existsByBusinessRegistrationNumber(validBusinessNumber)) {
            throw new GeneralException(SignupErrorStatus.BUSINESS_REGISTRATION_NUMBER_ALREADY_EXISTS);
        }
    }
}
