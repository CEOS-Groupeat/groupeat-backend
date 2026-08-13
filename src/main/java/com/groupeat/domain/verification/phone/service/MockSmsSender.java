package com.groupeat.domain.verification.phone.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsSender implements SmsSender {

    @Override
    public void sendVerificationCode(String phoneNumber, String code) {
        log.info("[MOCK SMS] phoneNumber={}, verificationCode={}", phoneNumber, code);
    }
}
