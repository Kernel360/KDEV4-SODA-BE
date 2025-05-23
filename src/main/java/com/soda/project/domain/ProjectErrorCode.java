package com.soda.project.domain;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ProjectErrorCode implements ErrorCode {
    COMPANY_PROJECT_NOT_FOUND("1001", "Company not found: The specified company does not exist.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("1002", "Member not found: The specified member does not exist.", HttpStatus.NOT_FOUND),
    INVALID_MEMBER_COMPANY("1003", "Invalid member company: The member does not belong to the specified company.", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND("1004", "Invalid project Id", HttpStatus.NOT_FOUND),
    INVALID_STAGE_FOR_PROJECT("1005", "Invalid stage for project", HttpStatus.NOT_FOUND),
    MEMBER_NOT_IN_PROJECT("1006", "This member does not exist in this project", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE("1007","Invalid date range: The end date cannot be before the start date" ,HttpStatus.NOT_FOUND ),
    UNAUTHORIZED_USER("1008", "Unauthorized: Only ADMIN users can create a project.", HttpStatus.FORBIDDEN),
    NO_PERMISSION_TO_UPDATE_STATUS("1009", "Unauthorized: You do not have permission to update the project status.", HttpStatus.FORBIDDEN),
    MEMBER_LIST_EMPTY("1010", "Either manager or participant member list must be provided.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_IN_SPECIFIED_COMPANY ("1011", "One or more requested members do not belong to the specified company.", HttpStatus.BAD_REQUEST),
    MEMBER_PROJECT_NOT_FOUND("1012", "Invalid member project", HttpStatus.NOT_FOUND),
    NOT_NULL("1013", "프로젝트, 회사, 역할은 null일 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ProjectErrorCode (String code, String message, HttpStatus httpStatus) {
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
