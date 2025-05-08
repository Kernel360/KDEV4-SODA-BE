package com.soda.project.domain.stage;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StageErrorCode implements ErrorCode {
    STAGE_NOT_FOUND("2401", "단계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STAGE_LIMIT_EXCEEDED("2402", "프로젝트당 최대 단계 개수를 초과했습니다.", HttpStatus.BAD_REQUEST),
    STAGE_PROJECT_MISMATCH("2403", "단계가 해당 프로젝트에 속하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_STAGE_ORDER("2404", "유효하지 않은 단계 순서입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_STAGE_NAME("2405", "이미 존재하는 단계 이름입니다.", HttpStatus.BAD_REQUEST),
    INVALID_STAGE_NAME("2406", "유효하지 않은 단계 이름입니다.", HttpStatus.BAD_REQUEST);

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
