package com.groupeat.domain.business.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorStatus implements BaseErrorCode {

    // 국세청 통신 관련 에러 추가
    NTS_API_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY, "BUSINESS5020", "국세청 서버와 통신 중 오류가 발생했습니다."),
    INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "BUSINESS4001", "국세청에 등록되지 않은 사업자번호입니다."),
    CLOSED_BUSINESS_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BUSINESS4002", "휴업 또는 폐업 상태의 사업자는 가입할 수 없습니다."),
    BUSINESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "BUSINESS4011", "사업자 검증 토큰이 만료되었습니다. 다시 인증해주세요."),
    INVALID_BUSINESS_TOKEN(HttpStatus.UNAUTHORIZED, "BUSINESS4012", "유효하지 않거나 변조된 사업자 검증 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message).code(code).isSuccess(false).build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message).code(code).isSuccess(false).httpStatus(httpStatus).build();
    }
}
