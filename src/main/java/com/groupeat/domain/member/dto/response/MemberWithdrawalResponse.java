package com.groupeat.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberWithdrawalResponse(
        @Schema(description = "처리 결과 메시지", example = "회원 탈퇴가 완료되었습니다.")
        String message
) {
}
