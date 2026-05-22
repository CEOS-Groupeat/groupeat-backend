package com.groupeat.domain.verification.phone.dto;

public record PhoneVerificationResponse(
        boolean verified,
        String message
) {
}
