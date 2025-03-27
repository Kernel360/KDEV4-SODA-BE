package com.soda.article.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CommentErrorCode implements ErrorCode {
    PARENT_COMMENT_NOT_FOUND("1021", "This comment does not have parent comment", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("1022", "This comment does not exist", HttpStatus.NOT_FOUND );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    CommentErrorCode (String code, String message, HttpStatus httpStatus) {
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
