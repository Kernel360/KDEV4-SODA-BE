package com.soda.project.domain.stage.common.file;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum FileErrorCode implements ErrorCode {
    FILE_DOMAIN_NOT_FOUND("3201", "The domain on this file does not exist", HttpStatus.NOT_FOUND),
    FILE_LIST_EMPTY("3202", "The list of files is empty" , HttpStatus.BAD_REQUEST ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    FileErrorCode (String code, String message, HttpStatus httpStatus) {
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
