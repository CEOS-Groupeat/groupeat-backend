package com.groupeat.domain.member.enums;

public enum MemberStatus {
    SIGNUP_IN_PROGRESS,  // 회원가입 진행 중
    ACTIVE,              // 정상 이용 가능
    BUSINESS_PENDING,    // 사업자 승인 대기
    BUSINESS_REJECTED,   // 사업자 인증 반려
    WITHDRAWN            // 탈퇴
}
