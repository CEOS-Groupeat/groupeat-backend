package com.groupeat.domain.auth.jwt;

import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;

public record AuthenticatedMember(
        Long memberId,
        MemberType memberType,
        MemberStatus memberStatus
) {
}
