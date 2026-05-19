package com.groupeat.domain.member.repository;

import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );
}
