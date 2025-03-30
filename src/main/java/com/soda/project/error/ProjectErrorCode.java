package com.soda.project.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ProjectErrorCode implements ErrorCode {
    COMPANY_NOT_FOUND("1001", "Company not found: The specified company does not exist.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("1002", "Member not found: The specified member does not exist.", HttpStatus.NOT_FOUND),
    INVALID_MEMBER_COMPANY("1003", "Invalid member company: The member does not belong to the specified company.", HttpStatus.BAD_REQUEST),
    PROJECT_TITLE_DUPLICATED("1004", "Project title is duplicated: A project with the same title already exists.", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND("1005", "Invalid project Id", HttpStatus.NOT_FOUND),
    PROJECT_ALREADY_DELETED("1006", "This Project is already deleted", HttpStatus.NOT_FOUND),
    INVALID_STAGE_FOR_PROJECT("1007", "Invalid stage for project", HttpStatus.NOT_FOUND),
    MEMBER_NOT_IN_PROJECT("1008", "This member does not exist in this project", HttpStatus.NOT_FOUND),
    ;

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
