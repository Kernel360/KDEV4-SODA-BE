package com.soda.project.domain.stage;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StageErrorCode implements ErrorCode {
    STAGE_NOT_FOUND("2401", "단계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STAGE_LIMIT_EXCEEDED("2402", "프로젝트당 최대 단계 개수를 초과했습니다.", HttpStatus.BAD_REQUEST),
    STAGE_PROJECT_MISMATCH("2403", "단계가 해당 프로젝트에 속하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_STAGE_ORDER("2404", "유효하지 않은 단계 순서입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_STAGE_NAME("2405", "이미 존재하는 단계 이름입니다.", HttpStatus.BAD_REQUEST),
    INVALID_STAGE_NAME("2406", "유효하지 않은 단계 이름입니다.", HttpStatus.BAD_REQUEST),
    STAGE_ID_REQUIRED("2407", "단계 ID는 필수입니다.", HttpStatus.BAD_REQUEST),
    STAGE_NAME_REQUIRED("2408", "단계 이름은 필수입니다.", HttpStatus.BAD_REQUEST),
    STAGE_NAME_TOO_LONG("2409", "단계 이름이 너무 깁니다.", HttpStatus.BAD_REQUEST),
    CANNOT_MOVE_STAGE_RELATIVE_TO_ITSELF("2410", "단계를 자기 자신을 기준으로 이동할 수 없습니다.", HttpStatus.BAD_REQUEST),
    PROJECT_ID_FOR_STAGE_OPERATION_REQUIRED("2411", "단계 작업을 위한 프로젝트 ID는 필수입니다.", HttpStatus.BAD_REQUEST);

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
