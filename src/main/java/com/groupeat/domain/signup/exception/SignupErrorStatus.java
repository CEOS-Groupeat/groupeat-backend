package com.groupeat.domain.signup.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SignupErrorStatus implements BaseErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "SIGNUP4040", "존재하지 않는 회원입니다."),
    SOCIAL_ACCOUNT_ALREADY_REGISTERED(HttpStatus.CONFLICT, "SIGNUP4090", "이미 가입된 소셜 계정입니다."),
    PHONE_NUMBER_ALREADY_REGISTERED(HttpStatus.CONFLICT, "SIGNUP4091", "이미 가입된 휴대폰 번호입니다."),
    NOT_CUSTOMER_MEMBER(HttpStatus.BAD_REQUEST, "SIGNUP4000", "고객 회원이 아닙니다."),
    NOT_BUSINESS_MEMBER(HttpStatus.BAD_REQUEST, "SIGNUP4002", "사업자 회원이 아닙니다."),
    SIGNUP_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "SIGNUP4001", "회원가입을 진행할 수 없는 상태입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "SIGNUP4093", "이미 사용 중인 이메일입니다."),
    BUSINESS_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "SIGNUP4094", "이미 사업자 정보가 등록된 회원입니다."),
    BUSINESS_REGISTRATION_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "SIGNUP4095", "이미 등록된 사업자등록번호입니다.");

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
