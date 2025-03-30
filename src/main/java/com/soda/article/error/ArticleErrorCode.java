package com.soda.article.error;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ArticleErrorCode implements ErrorCode {
    INVALID_INPUT("1101", "Invalid Input", HttpStatus.BAD_REQUEST),
    ARTICLE_ALREADY_DELETED("1102","This article is already deleted", HttpStatus.NOT_FOUND),
    INVALID_ARTICLE("1103", "The Article does not exist", HttpStatus.NOT_FOUND),
    PARENT_ARTICLE_NOT_FOUND("1104", "This parent article does not exist", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ArticleErrorCode (String code, String message, HttpStatus httpStatus) {
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
