package com.groupeat.domain.verification.phone.service;

public interface SmsSender {

    void sendVerificationCode(String phoneNumber, String code);
}
