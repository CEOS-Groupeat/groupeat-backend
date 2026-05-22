package com.groupeat.domain.signup.dto;

import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;

public record CustomerSignupResponse(
        Long memberId,
        MemberType memberType,
        MemberStatus memberStatus,
        String message
) {
}
