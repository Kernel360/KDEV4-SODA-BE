package com.soda.request.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ResponseErrorCode implements ErrorCode {
    RESPONSE_NOT_FOUND("3100", "This response id is not found in Requests", HttpStatus.NOT_FOUND),
    USER_NOT_WRITE_RESPONSE("3101", "This user is not write response", HttpStatus.UNAUTHORIZED),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ResponseErrorCode (String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
