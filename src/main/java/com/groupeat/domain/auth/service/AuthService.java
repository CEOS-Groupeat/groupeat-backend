package com.groupeat.domain.auth.service;

import com.groupeat.domain.auth.jwt.AuthTokenProvider;
import com.groupeat.domain.auth.jwt.SignupTokenProvider;
import com.groupeat.domain.auth.oauth.dto.OAuth2LoginUserInfo;
import com.groupeat.domain.auth.exception.AuthErrorStatus;
import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.global.exception.GeneralException;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.member.repository.SocialAccountRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final SignupTokenProvider signupTokenProvider;
    private final AuthTokenProvider authTokenProvider;
    private final AuthCookieService authCookieService;

    public Optional<Member> findMemberBySocialAccount(OAuth2LoginUserInfo userInfo) {
        return socialAccountRepository.findByProviderAndProviderUserId(
                        userInfo.provider(),
                        userInfo.providerUserId()
                )
                .flatMap(socialAccount -> memberRepository.findById(socialAccount.getMemberId()));
    }

    public String createSignupToken(OAuth2LoginUserInfo userInfo, MemberType memberType) {
        return signupTokenProvider.createSignupToken(userInfo, memberType);
    }

    public void reissueAccessToken(String refreshToken, HttpServletResponse response) {
        Long memberId = authTokenProvider.parseRefreshTokenMemberId(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(AuthErrorStatus.INACTIVE_MEMBER);
        }

        authCookieService.addAuthTokenCookies(response, member);
    }
}
