package com.groupeat.domain.verification.phone.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PhoneVerificationErrorStatus implements BaseErrorCode {

    VERIFICATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "PHONE4040", "휴대폰 인증 요청 내역이 없습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "PHONE4000", "인증번호가 만료되었습니다."),
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "PHONE4001", "인증번호가 일치하지 않습니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "PHONE4002", "휴대폰 인증이 완료되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
