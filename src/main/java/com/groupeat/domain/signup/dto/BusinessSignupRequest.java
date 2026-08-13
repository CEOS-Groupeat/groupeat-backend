package com.groupeat.domain.signup.dto;

import com.groupeat.domain.business.enums.BusinessType;
import com.groupeat.domain.member.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record BusinessSignupRequest(

        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotEmpty(message = "약관 동의 정보는 필수입니다.")
        List<@Valid SignupAgreementRequest> agreements,

        @NotNull(message = "사업자 유형은 필수입니다.")
        BusinessType businessType,

        @NotBlank(message = "대표자명은 필수입니다.")
        String representativeName,

        @NotBlank(message = "상호명은 필수입니다.")
        String businessName,

        @NotNull(message = "개업연월일은 필수입니다.")
        @PastOrPresent(message = "개업연월일은 미래일 수 없습니다.")
        LocalDate openedDate,

        @NotBlank(message = "사업자 인증 토큰은 필수입니다.")
        String businessValidationToken,

        @NotBlank(message = "사업자등록증 URL은 필수입니다.")
        String businessRegistrationCertificateUrl,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @PastOrPresent(message = "생년월일은 미래일 수 없습니다.")
        LocalDate birthDate,

        Gender gender
) {
}
