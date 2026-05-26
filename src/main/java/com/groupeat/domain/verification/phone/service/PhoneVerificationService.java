package com.groupeat.domain.verification.phone.service;

import com.groupeat.domain.verification.phone.dto.PhoneVerificationConfirmRequest;
import com.groupeat.domain.verification.phone.dto.PhoneVerificationResponse;
import com.groupeat.domain.verification.phone.dto.PhoneVerificationSendRequest;
import com.groupeat.domain.verification.phone.entity.PhoneVerification;
import com.groupeat.domain.verification.phone.exception.PhoneVerificationErrorStatus;
import com.groupeat.domain.verification.phone.repository.PhoneVerificationRepository;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class PhoneVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_BOUND = 1_000_000;

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final SmsSender smsSender;

    public PhoneVerificationResponse sendCode(PhoneVerificationSendRequest request) {
        String code = generateVerificationCode();

        PhoneVerification verification = PhoneVerification.create(
                request.phoneNumber(),
                code
        );

        phoneVerificationRepository.save(verification);
        smsSender.sendVerificationCode(request.phoneNumber(), code);

        return new PhoneVerificationResponse(
                false,
                "인증번호가 발송되었습니다. 개발 환경 인증번호는 " + code + "입니다."
        );
    }

    public PhoneVerificationResponse confirmCode(PhoneVerificationConfirmRequest request) {
        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneNumberOrderByIdDesc(request.phoneNumber())
                .orElseThrow(() -> new GeneralException(
                        PhoneVerificationErrorStatus.VERIFICATION_REQUEST_NOT_FOUND
                ));

        if (verification.isExpired()) {
            throw new GeneralException(PhoneVerificationErrorStatus.VERIFICATION_CODE_EXPIRED);
        }

        if (!verification.isCodeMatched(request.code())) {
            throw new GeneralException(PhoneVerificationErrorStatus.VERIFICATION_CODE_MISMATCH);
        }

        verification.verify();

        return new PhoneVerificationResponse(
                true,
                "휴대폰 인증이 완료되었습니다."
        );
    }

    public void validateVerifiedPhoneNumber(String phoneNumber) {
        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .orElseThrow(() -> new GeneralException(
                        PhoneVerificationErrorStatus.VERIFICATION_REQUEST_NOT_FOUND
                ));

        if (!verification.isVerified() || verification.isExpired() || verification.isUsed()) {
            throw new GeneralException(PhoneVerificationErrorStatus.PHONE_NOT_VERIFIED);
        }

        verification.use();
    }

    private String generateVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(CODE_BOUND));
    }
}
