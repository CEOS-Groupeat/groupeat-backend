package com.groupeat.domain.member.service;

import com.groupeat.domain.member.dto.request.CustomerAccountUpdateRequest;
import com.groupeat.domain.member.dto.request.PhoneNumberUpdateRequest;
import com.groupeat.domain.member.dto.response.CustomerAccountResponse;
import com.groupeat.domain.member.dto.response.CustomerMyPageResponse;
import com.groupeat.domain.member.dto.response.PhoneNumberUpdateResponse;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.verification.phone.service.PhoneVerificationService;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerMyPageService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final OrderRepository orderRepository;
    private final PhoneVerificationService phoneVerificationService;

    public CustomerMyPageResponse getMyPage(Long memberId) {
        getActiveCustomer(memberId);
        return CustomerMyPageResponse.of(orderRepository.countByMemberId(memberId));
    }

    public CustomerAccountResponse getAccount(Long memberId) {
        Member member = getActiveCustomer(memberId);
        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId).orElse(null);
        return CustomerAccountResponse.from(member, socialAccount);
    }

    @Transactional
    public CustomerAccountResponse updateAccount(Long memberId, CustomerAccountUpdateRequest request) {
        Member member = getActiveCustomer(memberId);
        String email = normalizeOptionalText(request.email());

        if (email != null && memberRepository.existsByEmailAndIdNot(email, memberId)) {
            throw new GeneralException(MemberErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        member.updateAccount(
                request.name().trim(),
                email,
                request.birthDate(),
                request.gender()
        );

        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId).orElse(null);
        return CustomerAccountResponse.from(member, socialAccount);
    }

    @Transactional
    public PhoneNumberUpdateResponse updatePhoneNumber(Long memberId, PhoneNumberUpdateRequest request) {
        Member member = getActiveCustomer(memberId);
        String phoneNumber = request.phoneNumber().trim();

        if (member.getPhoneNumber().equals(phoneNumber)) {
            throw new GeneralException(MemberErrorStatus.SAME_PHONE_NUMBER);
        }
        if (memberRepository.existsByPhoneNumberAndIdNot(phoneNumber, memberId)) {
            throw new GeneralException(MemberErrorStatus.PHONE_NUMBER_ALREADY_EXISTS);
        }

        // 변경 대상 번호의 인증 완료 여부 확인 및 인증 정보 사용 처리
        phoneVerificationService.validateVerifiedPhoneNumber(phoneNumber);
        member.updatePhoneNumber(phoneNumber);

        return new PhoneNumberUpdateResponse(phoneNumber, "휴대폰 번호가 변경되었습니다.");
    }

    // 고객 마이페이지 공통 접근 조건 검증
    public Member getActiveCustomer(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (!member.isCustomer()) {
            throw new GeneralException(MemberErrorStatus.NOT_CUSTOMER_MEMBER);
        }
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }
        return member;
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
