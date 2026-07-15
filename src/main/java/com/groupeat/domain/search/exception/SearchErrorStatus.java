package com.groupeat.domain.search.exception;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SearchErrorStatus implements BaseErrorCode {

    INVALID_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "SEARCH4000", "잘못된 검색 조건입니다."),
    PICKUP_DATE_IN_PAST(HttpStatus.BAD_REQUEST, "SEARCH4001", "픽업 날짜는 오늘 이후여야 합니다."),
    PICKUP_DATE_REQUIRED_FOR_TIME(HttpStatus.BAD_REQUEST, "SEARCH4002", "픽업 시간을 검색하려면 픽업 날짜를 함께 선택해야 합니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "SEARCH4003", "주문 수량은 1개 이상이어야 합니다."),
    INVALID_BUDGET(HttpStatus.BAD_REQUEST, "SEARCH4004", "예산은 0원 이상이어야 합니다."),
    PICKUP_TIME_IN_PAST(HttpStatus.BAD_REQUEST, "SEARCH4005", "오늘 날짜인 경우, 픽업 시간은 현재 시간 이후여야 합니다."),
    SEARCH_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "SEARCH4040", "조건에 맞는 가게를 찾을 수 없습니다.");

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
