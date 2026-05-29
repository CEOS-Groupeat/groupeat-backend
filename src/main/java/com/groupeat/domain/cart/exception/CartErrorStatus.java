package com.groupeat.domain.cart.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CartErrorStatus implements BaseErrorCode {

    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART4040", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART4041", "장바구니에 해당 메뉴가 존재하지 않습니다."),
    EMPTY_CART_SELECTION(HttpStatus.BAD_REQUEST, "CART4001", "담은 메뉴가 없습니다. 메뉴를 선택해주세요."),
    MULTIPLE_STORE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CART4002", "다른 가게의 메뉴는 동시에 주문할 수 없습니다."),
    DIFFERENT_PICKUP_TIME(HttpStatus.BAD_REQUEST, "CART4003", "선택한 장바구니 항목들의 픽업 날짜 및 시간이 일치하지 않습니다. 하나의 주문으로 묶을 수 없습니다.");

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