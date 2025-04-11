package com.soda.common.link.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum LinkErrorCode implements ErrorCode {
    LINK_DOMAIN_NOT_FOUND("3301", "The domain on this file does not exist", HttpStatus.NOT_FOUND),
    FILE_LIST_EMPTY("3302", "The file list is empty" , HttpStatus.BAD_REQUEST ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    LinkErrorCode (String code, String message, HttpStatus httpStatus) {
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