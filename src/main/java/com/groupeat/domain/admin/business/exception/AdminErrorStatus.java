package com.groupeat.domain.admin.business.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdminErrorStatus implements BaseErrorCode {

    ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ADMIN4030", "해당 기능을 사용할 수 있는 관리자 권한이 없습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN4040", "해당 사업자 인증 요청 프로필을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN4041", "사업자 인증과 연결된 회원 정보를 찾을 수 없습니다."),

    ALREADY_PROCESSED_VERIFICATION(HttpStatus.BAD_REQUEST, "ADMIN4000", "이미 승인되거나 반려된 사업자 인증 요청입니다."),
    REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "ADMIN4001", "반려 처리 시 반려 사유는 필수적으로 입력해야 합니다.");

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
