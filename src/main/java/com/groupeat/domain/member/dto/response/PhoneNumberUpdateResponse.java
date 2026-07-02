package com.groupeat.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PhoneNumberUpdateResponse(
        @Schema(description = "변경된 휴대폰 번호", example = "01098765432")
        String phoneNumber,

        @Schema(description = "처리 결과 메시지", example = "휴대폰 번호가 변경되었습니다.")
        String message
) {
}
