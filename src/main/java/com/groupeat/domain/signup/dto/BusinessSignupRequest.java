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

        @NotBlank(message = "사업자등록번호는 필수입니다.")
        String businessRegistrationNumber,

        @NotBlank(message = "사업자등록증 URL은 필수입니다.")
        String businessRegistrationCertificateUrl,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotNull(message = "나이는 필수입니다.")
        @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
        Integer age,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender
) {
}
