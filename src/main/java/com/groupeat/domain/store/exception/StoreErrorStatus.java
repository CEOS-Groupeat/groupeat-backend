package com.groupeat.domain.store.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreErrorStatus implements BaseErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE4040", "해당 가게를 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE4041", "메뉴를 찾을 수 없습니다."),
    STORE_CLOSED(HttpStatus.BAD_REQUEST, "STORE4000", "현재 영업 중인 가게가 아닙니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "STORE4001", "지원하지 않는 카테고리입니다."),
    INVALID_REGION(HttpStatus.BAD_REQUEST, "STORE4002", "지원하지 않는 지역입니다."),
    INVALID_MENU_OPTION(HttpStatus.BAD_REQUEST, "STORE4003", "유효하지 않은 메뉴 옵션입니다."),
    INVALID_MENU_OPTION_MAPPING(HttpStatus.BAD_REQUEST, "STORE4004", "해당 메뉴에 속하지 않는 옵션입니다.");

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
