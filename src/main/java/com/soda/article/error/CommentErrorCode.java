package com.soda.article.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CommentErrorCode implements ErrorCode {
    PARENT_COMMENT_NOT_FOUND("1201", "부모 댓글이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("1202", "해당 댓글은 존재하지 않습니다.", HttpStatus.NOT_FOUND ),
    FORBIDDEN_ACTION("1203", "해당 댓글을 수정/삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);

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
