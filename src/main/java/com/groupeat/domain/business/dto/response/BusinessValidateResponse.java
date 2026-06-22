package com.groupeat.domain.business.dto.response;

public record BusinessValidateResponse(
        String validationToken // 검증 성공 시 발급되는 JWT 토큰
) {
}
