package com.groupeat.domain.auth.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    MISSING_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4010", "Refresh token이 없습니다."),
    MISSING_MEMBER_TYPE(HttpStatus.BAD_REQUEST, "AUTH4000", "회원 유형 정보가 없습니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH4001", "지원하지 않는 소셜 로그인입니다."),
    INVALID_OAUTH_USER_INFO(HttpStatus.BAD_REQUEST, "AUTH4002", "소셜 사용자 정보를 확인할 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4011", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "만료된 토큰입니다."),
    NOT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "Access token이 아닙니다."),
    NOT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4014", "Refresh token이 아닙니다."),
    INACTIVE_MEMBER(HttpStatus.FORBIDDEN, "AUTH4030", "활성 회원이 아닙니다.");

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
