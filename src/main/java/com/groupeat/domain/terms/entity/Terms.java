package com.groupeat.domain.terms.entity;

import com.groupeat.domain.terms.enums.TermsTargetType;
import com.groupeat.domain.terms.enums.TermsType;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약관 제목
     * 예: 서비스 이용약관, 개인정보 처리방침, 마케팅 정보 수신 동의
     */
    @Column(nullable = false)
    private String title;

    /**
     * 약관 본문
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 필수 약관 여부
     */
    @Column(nullable = false)
    private boolean required;

    /**
     * 약관 유형
     * 기존 데이터 호환을 위해 nullable로 관리한다.
     */
    @Enumerated(EnumType.STRING)
    private TermsType type;

    /**
     * 약관 대상
     * COMMON: 공통 약관
     * CUSTOMER: 고객 추가 약관
     * BUSINESS: 사업자 추가 약관
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermsTargetType targetType;

    /**
     * 약관 버전
     * 예: 1.0
     */
    @Column(nullable = false)
    private String version;

    /**
     * 현재 사용 중인 약관 여부
     */
    @Column(nullable = false)
    private boolean active;
}
