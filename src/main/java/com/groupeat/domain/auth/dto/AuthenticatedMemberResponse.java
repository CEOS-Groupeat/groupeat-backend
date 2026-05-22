package com.groupeat.domain.auth.dto;

import com.groupeat.domain.auth.jwt.AuthenticatedMember;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;

public record AuthenticatedMemberResponse(
        Long memberId,
        MemberType memberType,
        MemberStatus memberStatus
) {

    public static AuthenticatedMemberResponse from(AuthenticatedMember member) {
        return new AuthenticatedMemberResponse(
                member.memberId(),
                member.memberType(),
                member.memberStatus()
        );
    }
}
