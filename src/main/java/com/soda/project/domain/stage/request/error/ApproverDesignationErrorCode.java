package com.soda.project.domain.stage.request.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ApproverDesignationErrorCode implements ErrorCode {

    APPROVE_NOT_FOUND("3401", "ApproveDesignation not found", HttpStatus.NOT_FOUND),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ApproverDesignationErrorCode (String code, String message, HttpStatus httpStatus) {
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
