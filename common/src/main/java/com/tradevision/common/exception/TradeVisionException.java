package com.tradevision.common.exception;

import org.springframework.http.HttpStatus;

public class TradeVisionException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public TradeVisionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = status.name();
    }

    public TradeVisionException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public TradeVisionException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = status.name();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
