package com.soda.project.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StageErrorCode implements ErrorCode {
    STAGE_NOT_FOUND("2400", "존재하지 않는 단계입니다.", HttpStatus.NOT_FOUND),
    STAGE_LIMIT_EXCEEDED("2401", "최대 단계 개수(10개)를 초과했습니다.", HttpStatus.BAD_REQUEST),
    STAGE_PROJECT_MISMATCH("2402", "참조된 단계가 현재 프로젝트 소속이 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_STAGE_ORDER("2403", "유효하지 않은 단계 순서 설정입니다.", HttpStatus.BAD_REQUEST)
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
