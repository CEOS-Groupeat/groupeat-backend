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
    DIFFERENT_PICKUP_TIME(HttpStatus.BAD_REQUEST, "CART4003", "선택한 장바구니 항목들의 픽업 날짜 및 시간이 일치하지 않습니다. 하나의 주문으로 묶을 수 없습니다."),
    CART_DATETIME_MISMATCH(HttpStatus.BAD_REQUEST, "CART4004", "장바구니에 담긴 메뉴와 픽업 날짜 및 시간이 일치하지 않습니다."),
    PICKUP_TIME_IN_PAST(HttpStatus.BAD_REQUEST, "CART4005", "픽업 날짜 및 시간은 현재 시간 이후여야 합니다."),
    PICKUP_DATE_BEFORE_LEAD_TIME(HttpStatus.BAD_REQUEST, "CART4006", "가게의 최소 주문 가능 기한(예약 Lead Time)을 충족하지 않았습니다."),
    STORE_SCHEDULE_NOT_FOUND(HttpStatus.BAD_REQUEST, "CART4007", "해당 날짜에 활성화된 가게 영업 스케줄이 존재하지 않습니다."),
    STORE_NOT_AVAILABLE_ON_DAY(HttpStatus.BAD_REQUEST, "CART4008", "해당 요일 및 시간은 가게 영업/픽업 가능 시간이 아니거나 브레이크 타임입니다."),
    MIN_ORDER_QUANTITY_NOT_SATISFIED(HttpStatus.BAD_REQUEST, "CART4009", "가게의 최소 주문 가능 수량보다 적게 담을 수 없습니다."),
    MAX_ORDER_QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "CART4010", "가게의 최대 주문 가능 수량을 초과하였습니다.");

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
