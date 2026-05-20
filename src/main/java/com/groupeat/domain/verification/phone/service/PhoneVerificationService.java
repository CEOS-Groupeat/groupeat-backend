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

@Service
@RequiredArgsConstructor
@Transactional
public class PhoneVerificationService {

    private static final String MOCK_CODE = "123456"; // 일단은 mock code 전송

    private final PhoneVerificationRepository phoneVerificationRepository;

    public PhoneVerificationResponse sendCode(PhoneVerificationSendRequest request) {
        PhoneVerification verification = PhoneVerification.create(
                request.phoneNumber(),
                MOCK_CODE
        );

        phoneVerificationRepository.save(verification);

        return new PhoneVerificationResponse(
                false,
                "인증번호가 발송되었습니다. 개발 환경 인증번호는 123456입니다."
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

    @Transactional(readOnly = true)
    public void validateVerifiedPhoneNumber(String phoneNumber) {
        PhoneVerification verification = phoneVerificationRepository
                .findTopByPhoneNumberOrderByIdDesc(phoneNumber)
                .orElseThrow(() -> new GeneralException(
                        PhoneVerificationErrorStatus.VERIFICATION_REQUEST_NOT_FOUND
                ));

        if (!verification.isVerified() || verification.isExpired()) {
            throw new GeneralException(PhoneVerificationErrorStatus.PHONE_NOT_VERIFIED);
        }
    }
}
