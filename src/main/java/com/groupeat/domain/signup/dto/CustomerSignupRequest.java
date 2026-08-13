package com.groupeat.domain.signup.dto;

import com.groupeat.domain.member.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record CustomerSignupRequest(

        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @NotEmpty(message = "약관 동의 정보는 필수입니다.")
        List<@Valid SignupAgreementRequest> agreements,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @PastOrPresent(message = "생년월일은 미래일 수 없습니다.")
        LocalDate birthDate,

        Gender gender
) {
}
