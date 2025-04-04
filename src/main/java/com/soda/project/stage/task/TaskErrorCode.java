package com.soda.project.stage.task;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TaskErrorCode implements ErrorCode {

    TASK_NOT_FOUND("2501", "존재하지 않는 태스크입니다.", HttpStatus.NOT_FOUND),
    TASK_STAGE_MISMATCH("2502", "참조된 태스크가 현재 스테이지 소속이 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_TASK_ORDER("2503", "유효하지 않은 태스크 순서 설정입니다.", HttpStatus.BAD_REQUEST)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    TaskErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() { return code; }
    @Override
    public String getMessage() { return message; }
    @Override
    public HttpStatus getHttpStatus() { return httpStatus; }
}
