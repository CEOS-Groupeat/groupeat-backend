package com.groupeat.domain.member.dto.request;

import com.groupeat.domain.member.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CustomerAccountUpdateRequest(
        @Schema(description = "변경할 이메일. 빈 문자열 전송 시 null로 저장", example = "gildong@example.com", nullable = true)
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "변경할 생년월일. 미입력 시 null로 저장", example = "1998-03-15", nullable = true)
        @PastOrPresent(message = "생년월일은 미래일 수 없습니다.")
        LocalDate birthDate,

        @Schema(description = "변경할 성별. 미입력 시 null로 저장", example = "MALE", nullable = true)
        Gender gender
) {
}
