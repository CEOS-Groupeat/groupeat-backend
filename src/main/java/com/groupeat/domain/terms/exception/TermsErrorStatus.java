package com.groupeat.domain.terms.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TermsErrorStatus implements BaseErrorCode {

    REQUIRED_TERMS_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "TERMS5000", "활성화된 필수 약관이 존재하지 않습니다."),
    MARKETING_TERMS_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "TERMS5001", "활성화된 마케팅 정보 수신 동의 약관이 존재하지 않습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS4000", "필수 약관에 동의하지 않았습니다."),
    INVALID_TERMS_TARGET(HttpStatus.BAD_REQUEST, "TERMS4001", "약관 대상이 올바르지 않습니다."),
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS4040", "존재하지 않는 약관입니다."),
    TERMS_NOT_ACCESSIBLE(HttpStatus.FORBIDDEN, "TERMS4030", "고객이 조회할 수 없는 약관입니다."),
    REQUIRED_TERMS_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "TERMS4002", "필수 약관은 변경할 수 없습니다.");

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
