package com.groupeat.domain.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record OwnerWithdrawalResponse(
        @Schema(description = "처리 결과 메시지", example = "사업자 회원 탈퇴가 완료되었습니다.")
        String message
) {
}
