package com.groupeat.global.upload.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UploadErrorStatus implements BaseErrorCode {

    INVALID_IMAGE_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "UPLOAD4000", "지원하지 않는 이미지 형식입니다."),
    INVALID_IMAGE_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "UPLOAD4001", "지원하지 않는 이미지 확장자입니다.");

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
