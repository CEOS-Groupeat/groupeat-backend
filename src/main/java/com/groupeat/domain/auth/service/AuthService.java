package com.groupeat.domain.auth.service;

import com.groupeat.domain.auth.jwt.SignupTokenProvider;
import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final SocialAccountRepository socialAccountRepository;
    private final SignupTokenProvider signupTokenProvider;

    public boolean isRegisteredUser(OAuth2LoginUserInfo userInfo) {
        Optional<SocialAccount> socialAccount =
                socialAccountRepository.findByProviderAndProviderUserId(
                        userInfo.provider(),
                        userInfo.providerUserId()
                );

        return socialAccount.isPresent();
    }

    public String createSignupToken(OAuth2LoginUserInfo userInfo, MemberType memberType) {
        return signupTokenProvider.createSignupToken(userInfo, memberType);
    }
}
