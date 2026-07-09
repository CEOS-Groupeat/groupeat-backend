package com.groupeat.domain.owner.dto.response;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.entity.SocialAccount;
import com.groupeat.domain.member.enums.Gender;
import com.groupeat.domain.member.enums.OAuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record OwnerProfileResponse(
        @Schema(description = "실명", example = "홍길동")
        String name,

        @Schema(description = "휴대폰 번호", example = "01012345678")
        String phoneNumber,

        @Schema(description = "생년월일", example = "1988-03-15", nullable = true)
        LocalDate birthDate,

        @Schema(description = "회원 이메일", example = "owner@example.com", nullable = true)
        String email,

        @Schema(description = "성별", example = "MALE", nullable = true)
        Gender gender,

        @Schema(description = "연결된 소셜 계정 정보")
        SocialAccountResponse socialAccount
) {
    public static OwnerProfileResponse from(Member member, SocialAccount socialAccount) {
        return new OwnerProfileResponse(
                member.getName(),
                member.getPhoneNumber(),
                member.getBirthDate(),
                member.getEmail(),
                member.getGender(),
                SocialAccountResponse.from(socialAccount)
        );
    }

    public record SocialAccountResponse(
            @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
            OAuthProvider provider,

            @Schema(description = "소셜 계정 이메일. 제공되지 않은 경우 빈 문자열 반환", example = "")
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
