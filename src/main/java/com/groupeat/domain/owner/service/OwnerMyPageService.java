package com.groupeat.domain.owner.service;

import com.groupeat.domain.business.entity.BusinessProfile;
import com.groupeat.domain.business.exception.BusinessErrorStatus;
import com.groupeat.domain.business.repository.BusinessProfileRepository;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import com.groupeat.domain.owner.dto.request.OwnerProfileUpdateRequest;
import com.groupeat.domain.owner.dto.response.OwnerBusinessProfileResponse;
import com.groupeat.domain.owner.dto.response.OwnerProfileResponse;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerMyPageService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final BusinessProfileRepository businessProfileRepository;

    public OwnerProfileResponse getProfile(Long memberId) {
        Member member = getActiveBusinessOwner(memberId);
        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId).orElse(null);
        return OwnerProfileResponse.from(member, socialAccount);
    }

    public OwnerBusinessProfileResponse getBusinessProfile(Long memberId) {
        getActiveBusinessOwner(memberId);
        BusinessProfile businessProfile = businessProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new GeneralException(BusinessErrorStatus.BUSINESS_PROFILE_NOT_FOUND));
        return OwnerBusinessProfileResponse.from(businessProfile);
    }

    @Transactional
    public OwnerProfileResponse updateProfile(Long memberId, OwnerProfileUpdateRequest request) {
        Member member = getActiveBusinessOwner(memberId);
        String email = normalizeOptionalText(request.email());

        if (email != null && memberRepository.existsByEmailAndIdNot(email, memberId)) {
            throw new GeneralException(MemberErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        member.updateAccount(email, request.birthDate(), request.gender());

        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId).orElse(null);
        return OwnerProfileResponse.from(member, socialAccount);
    }

    public Member getActiveBusinessOwner(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (!member.isBusiness()) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
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
