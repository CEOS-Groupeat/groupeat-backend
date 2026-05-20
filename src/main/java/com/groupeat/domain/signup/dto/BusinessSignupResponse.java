package com.groupeat.domain.signup.dto;

import com.groupeat.domain.business.enums.BusinessVerificationStatus;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;

public record BusinessSignupResponse(
        Long memberId,
        MemberType memberType,
        MemberStatus memberStatus,
        BusinessVerificationStatus businessVerificationStatus,
        String message
) {
}
