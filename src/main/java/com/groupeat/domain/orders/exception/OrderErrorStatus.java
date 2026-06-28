package com.groupeat.domain.orders.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorStatus implements BaseErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER4040", "주문 내역을 찾을 수 없습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER4001", "결제 요청 금액이 실제 계산된 금액과 일치하지 않습니다."),
    ALREADY_PROCESSED_ORDER(HttpStatus.BAD_REQUEST, "ORDER4002", "이미 결제 처리되거나 취소된 주문입니다."),
    ORDER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "ORDER4003", "현재 상태에서는 주문을 취소할 수 없습니다."),
    ORDER_ACCEPT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "ORDER4004", "현재 상태에서는 주문을 승인할 수 없습니다."),
    ORDER_REJECT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "ORDER4005", "현재 상태에서는 주문을 거절할 수 없습니다."),
    ORDER_PICKUP_COMPLETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "ORDER4006", "현재 상태에서는 픽업 완료 처리할 수 없습니다."),
    ORDER_SCHEDULE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ORDER4007", "주문 가능한 일정이 아닙니다."),
    ORDER_QUANTITY_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "ORDER4008", "주문 가능한 수량을 초과했습니다."),
    BUSINESS_MEMBER_REQUIRED(HttpStatus.FORBIDDEN, "ORDER4030", "사업자 회원만 처리할 수 있는 주문 요청입니다.");

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
