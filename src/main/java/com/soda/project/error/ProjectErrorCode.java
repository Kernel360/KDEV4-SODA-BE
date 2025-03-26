package com.soda.project.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ProjectErrorCode implements ErrorCode {
    PROJECT_NOT_FOUND("1006", "Invalid project Id", HttpStatus.NOT_FOUND),
    STAGE_NOT_FOUND("1009", "The stage does not exist", HttpStatus.NOT_FOUND),
    INVALID_STAGE_FOR_PROJECT("1012", "Invalid stage for project", HttpStatus.NOT_FOUND),
    MEMBER_NOT_IN_PROJECT("1010", "This member does not exist in this project", HttpStatus.NOT_FOUND),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ProjectErrorCode (String code, String message, HttpStatus httpStatus) {
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
