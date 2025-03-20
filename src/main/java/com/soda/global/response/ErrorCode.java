package com.soda.global.response;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // HTTP 상태 코드 (4xx)
    BAD_REQUEST("400", "Bad Request: Invalid input or malformed request.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("401", "Unauthorized: You must authenticate to access this resource.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("403", "Forbidden: You do not have permission to access this resource.", HttpStatus.FORBIDDEN),
    NOT_FOUND("404", "Not Found: The requested resource could not be found.", HttpStatus.NOT_FOUND),

    // HTTP 상태 코드 (5xx)
    INTERNAL_SERVER_ERROR("500", "Internal Server Error: An unexpected error occurred on the server.", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_GATEWAY("502", "Bad Gateway: The server received an invalid response from the upstream server.", HttpStatus.BAD_GATEWAY),
    SERVICE_UNAVAILABLE("503", "Service Unavailable: The server is temporarily unable to handle the request.", HttpStatus.SERVICE_UNAVAILABLE),

    // service 로직 오류 메시지
    COMPANY_NOT_FOUND("1001", "Company not found: The specified company does not exist.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("1002", "Member not found: The specified member does not exist.", HttpStatus.NOT_FOUND),
    INVALID_MEMBER_COMPANY("1003", "Invalid member company: The member does not belong to the specified company.", HttpStatus.BAD_REQUEST),
    PROJECT_TITLE_DUPLICATED("1004", "Project title is duplicated: A project with the same title already exists.", HttpStatus.BAD_REQUEST),

    // 공통 오류 메시지
    UNEXPECTED_ERROR("1000", "Unexpected Error: An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    // 생성자
    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    // 코드와 메시지를 반환하는 메서드
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}