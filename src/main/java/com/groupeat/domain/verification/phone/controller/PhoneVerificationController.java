package com.groupeat.domain.verification.phone.controller;

import com.groupeat.domain.verification.phone.dto.PhoneVerificationConfirmRequest;
import com.groupeat.domain.verification.phone.dto.PhoneVerificationResponse;
import com.groupeat.domain.verification.phone.dto.PhoneVerificationSendRequest;
import com.groupeat.domain.verification.phone.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/phone-verifications")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/send")
    public PhoneVerificationResponse sendCode(
            @Valid @RequestBody PhoneVerificationSendRequest request
    ) {
        return phoneVerificationService.sendCode(request);
    }

    @PostMapping("/confirm")
    public PhoneVerificationResponse confirmCode(
            @Valid @RequestBody PhoneVerificationConfirmRequest request
    ) {
        return phoneVerificationService.confirmCode(request);
    }
}
