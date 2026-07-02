package com.groupeat.domain.member.dto.response;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.Gender;
import com.groupeat.domain.member.enums.OAuthProvider;

import java.time.LocalDate;

public record CustomerAccountResponse(
        String name,
        String phoneNumber,
        LocalDate birthDate,
        String email,
        Gender gender,
        SocialAccountResponse socialAccount
) {
    public static CustomerAccountResponse from(Member member, SocialAccount socialAccount) {
        return new CustomerAccountResponse(
                member.getName(),
                member.getPhoneNumber(),
                member.getBirthDate(),
                member.getEmail(),
                member.getGender(),
                SocialAccountResponse.from(socialAccount)
        );
    }

    public record SocialAccountResponse(
            OAuthProvider provider,
            String email
    ) {
        private static SocialAccountResponse from(SocialAccount socialAccount) {
            if (socialAccount == null) {
                return null;
            }
            return new SocialAccountResponse(
                    socialAccount.getProvider(),
                    socialAccount.getEmail() == null ? "" : socialAccount.getEmail()
            );
        }
    }
}
