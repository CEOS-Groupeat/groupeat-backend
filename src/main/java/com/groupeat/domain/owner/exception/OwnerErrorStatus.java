package com.groupeat.domain.owner.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OwnerErrorStatus implements BaseErrorCode {

    OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "OWNER4040", "사장님 계정을 찾을 수 없습니다."),
    DASHBOARD_DATA_NOT_AVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "OWNER5000", "대시보드 데이터를 불러오는 중 오류가 발생했습니다.");

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
