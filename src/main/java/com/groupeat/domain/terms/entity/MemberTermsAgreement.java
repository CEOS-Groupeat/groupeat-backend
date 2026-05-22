package com.groupeat.domain.terms.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_terms_agreement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MVP에서는 연관관계를 직접 매핑하지 않고 memberId로 관리한다.
     */
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long termsId;

    @Column(nullable = false)
    private boolean agreed;

    private LocalDateTime agreedAt;

    public static MemberTermsAgreement create(
            Long memberId,
            Long termsId,
            boolean agreed
    ) {
        MemberTermsAgreement agreement = new MemberTermsAgreement();
        agreement.memberId = memberId;
        agreement.termsId = termsId;
        agreement.agreed = agreed;
        agreement.agreedAt = agreed ? LocalDateTime.now() : null;
        return agreement;
    }
}
