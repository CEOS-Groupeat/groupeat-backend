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
    INVALID_MENU_OPTION_MAPPING(HttpStatus.BAD_REQUEST, "STORE4004", "해당 메뉴에 속하지 않는 옵션입니다."),
    MENU_NOT_MATCH_STORE(HttpStatus.BAD_REQUEST, "STORE4005", "요청하신 가게의 메뉴가 아닙니다."),
    BUSINESS_MEMBER_REQUIRED(HttpStatus.FORBIDDEN, "STORE4030", "사업자 회원만 접근할 수 있습니다."),
    ACTIVE_BUSINESS_MEMBER_REQUIRED(HttpStatus.FORBIDDEN, "STORE4031", "활성 사업자 회원만 접근할 수 있습니다."),
    OWNER_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE4042", "사업자 회원의 가게를 찾을 수 없습니다.");

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
