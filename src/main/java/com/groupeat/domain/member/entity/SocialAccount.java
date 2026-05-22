package com.groupeat.domain.member.entity;

import com.groupeat.domain.member.enums.OAuthProvider;
import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(
        name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_account_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연관관계를 단순하게 가져가기 위해 일단은 memberId로 관리한다.
     */
    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    private String email;

    public static SocialAccount create(
            Long memberId,
            OAuthProvider provider,
            String providerUserId,
            String email
    ) {
        SocialAccount socialAccount = new SocialAccount();
        socialAccount.memberId = memberId;
        socialAccount.provider = provider;
        socialAccount.providerUserId = providerUserId;
        socialAccount.email = email;
        return socialAccount;
    }
}
