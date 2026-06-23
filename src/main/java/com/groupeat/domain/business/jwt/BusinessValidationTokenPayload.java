package com.groupeat.domain.business.jwt;

public record BusinessValidationTokenPayload(
        String businessRegistrationNumber // 검증 통과된 10자리 사업자번호
) {
}
