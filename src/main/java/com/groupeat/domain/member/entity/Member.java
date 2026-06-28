package com.groupeat.domain.member.entity;

import com.groupeat.domain.member.enums.Gender;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원 유형
     * CUSTOMER: 고객
     * BUSINESS: 사업자
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType memberType;

    /**
     * 회원 상태
     * SIGNUP_IN_PROGRESS: 회원가입 진행 중
     * ACTIVE: 정상 회원
     * BUSINESS_PENDING: 사업자 승인 대기
     * BUSINESS_REJECTED: 사업자 인증 반려
     * WITHDRAWN: 탈퇴
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    private String name;

    @Column(unique = true)
    private String email;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String phoneNumber;

    public static Member createInProgress(
            MemberType memberType,
            String phoneNumber
    ) {
        Member member = new Member();
        member.memberType = memberType;
        member.memberStatus = MemberStatus.SIGNUP_IN_PROGRESS;
        member.phoneNumber = phoneNumber;
        return member;
    }

    public void completeCustomerSignup(
            String name,
            String email,
            LocalDate birthDate,
            Gender gender
    ) {
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
        this.memberStatus = MemberStatus.ACTIVE;
    }

    public void completeBusinessBasicInfo(
            String name,
            String email,
            LocalDate birthDate,
            Gender gender
    ) {
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
        this.memberStatus = MemberStatus.BUSINESS_PENDING;
    }

    public void approveBusiness() {
        this.memberStatus = MemberStatus.ACTIVE;
    }

    public void rejectBusiness() {
        this.memberStatus = MemberStatus.BUSINESS_REJECTED;
    }

    public boolean isCustomer() {
        return this.memberType == MemberType.CUSTOMER;
    }

    public boolean isBusiness() {
        return this.memberType == MemberType.BUSINESS;
    }
}
