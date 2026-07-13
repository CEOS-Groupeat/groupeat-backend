package com.groupeat.domain.member.exceptoin;

import com.groupeat.global.apiPayload.code.BaseErrorCode;
import com.groupeat.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorStatus implements BaseErrorCode {

    MEMBER_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "MEMBER4001", "정상 이용이 불가한 회원입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4040", "회원을 찾을 수 없습니다."),
    NOT_CUSTOMER_MEMBER(HttpStatus.FORBIDDEN, "MEMBER4030", "고객 회원만 이용할 수 있습니다."),
    NOT_BUSINESS_MEMBER(HttpStatus.FORBIDDEN, "MEMBER4031", "사업자 회원만 이용할 수 있습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER4090", "이미 사용 중인 이메일입니다."),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER4091", "이미 사용 중인 휴대폰 번호입니다."),
    SAME_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "MEMBER4002", "현재 휴대폰 번호와 동일합니다."),
    ACTIVE_ORDER_EXISTS(HttpStatus.CONFLICT, "MEMBER4092", "진행 중인 주문이 있어 탈퇴할 수 없습니다."),
    PENDING_SETTLEMENT_EXISTS(HttpStatus.CONFLICT, "MEMBER4093", "정산 대기 건이 있어 탈퇴할 수 없습니다.");

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
