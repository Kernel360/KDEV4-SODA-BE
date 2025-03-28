package com.soda.project.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StageErrorCode implements ErrorCode {
    STAGE_LIMIT_EXCEEDED("2401", "Stage limit exceeded (maximum 10)", HttpStatus.BAD_REQUEST),
    STAGE_NOT_FOUND("2402", "The stage does not exist", HttpStatus.NOT_FOUND),

    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    StageErrorCode (String code, String message, HttpStatus httpStatus) {
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
