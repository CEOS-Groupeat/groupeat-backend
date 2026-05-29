package com.groupeat.domain.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class TossPaymentException extends RuntimeException {

    private final HttpStatusCode httpStatusCode;
    private final String tossErrorCode;
    private final String tossErrorMessage;

    public TossPaymentException(HttpStatusCode httpStatusCode, String tossErrorCode, String tossErrorMessage) {
        super(tossErrorMessage);
        this.httpStatusCode = httpStatusCode;
        this.tossErrorCode = tossErrorCode;
        this.tossErrorMessage = tossErrorMessage;
    }
}
