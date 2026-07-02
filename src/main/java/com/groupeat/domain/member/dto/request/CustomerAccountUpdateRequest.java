package com.groupeat.domain.member.dto.request;

import com.groupeat.domain.member.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CustomerAccountUpdateRequest(
        @NotBlank(message = "실명은 필수입니다.")
        String name,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @PastOrPresent(message = "생년월일은 미래일 수 없습니다.")
        LocalDate birthDate,

        Gender gender
) {
}
