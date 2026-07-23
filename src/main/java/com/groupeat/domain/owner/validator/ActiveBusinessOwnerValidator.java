package com.groupeat.domain.owner.validator;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveBusinessOwnerValidator {

    private final MemberRepository memberRepository;

    public void validate(Long ownerId) {
        Member member = memberRepository.findById(ownerId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }

        if (member.getMemberType() != MemberType.BUSINESS) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
        }
    }
}
