package com.groupeat.domain.signup.dto;

import com.groupeat.domain.member.enums.MemberType;

public record CommonSignupResponse(
        Long memberId,
        MemberType memberType,
        String nextStep
) {
}
