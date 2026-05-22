package com.groupeat.domain.verification.phone.controller;

import com.groupeat.domain.verification.phone.dto.PhoneVerificationConfirmRequest;
import com.groupeat.domain.verification.phone.dto.PhoneVerificationResponse;
import com.groupeat.domain.verification.phone.dto.PhoneVerificationSendRequest;
import com.groupeat.domain.verification.phone.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/phone-verifications")
@RequiredArgsConstructor
@Tag(name = "Phone Verification", description = "휴대폰 본인 인증 API")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/send")
    @Operation(summary = "인증번호 발송", description = "휴대폰 번호로 인증번호를 발송합니다.")
    public PhoneVerificationResponse sendCode(
            @Valid @RequestBody PhoneVerificationSendRequest request
    ) {
        return phoneVerificationService.sendCode(request);
    }

    @PostMapping("/confirm")
    @Operation(summary = "인증번호 확인", description = "휴대폰 인증번호를 확인하고 인증 완료 처리합니다.")
    public PhoneVerificationResponse confirmCode(
            @Valid @RequestBody PhoneVerificationConfirmRequest request
    ) {
        return phoneVerificationService.confirmCode(request);
    }
}
