package com.groupeat.domain.review.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorStatus implements BaseErrorCode {

    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "REVIEW_4001", "해당 주문에 대한 리뷰가 이미 존재합니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "REVIEW_4002", "픽업이 완료된 주문만 리뷰를 작성할 수 있습니다."),
    UNAUTHORIZED_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "REVIEW_4003", "해당 주문의 리뷰를 작성할 권한이 없습니다."),

    INVALID_MENU_RATING(HttpStatus.BAD_REQUEST, "REVIEW_4004", "해당 주문에 포함되지 않은 메뉴의 별점입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_4005", "해당 리뷰를 찾을 수 없습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_4006", "해당 주문 내역을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_4007", "해당 주문 항목(메뉴)을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .isSuccess(false)
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}