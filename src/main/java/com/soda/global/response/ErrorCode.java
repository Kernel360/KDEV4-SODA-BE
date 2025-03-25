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

    // 인증 관련 추가 오류
    UNEXPECTED_ERROR("1000", "Unexpected Error: An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),

    // service 로직 오류 메시지
    COMPANY_NOT_FOUND("1001", "Company not found: The specified company does not exist.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("1002", "Member not found: The specified member does not exist.", HttpStatus.NOT_FOUND),
    INVALID_MEMBER_COMPANY("1003", "Invalid member company: The member does not belong to the specified company.", HttpStatus.BAD_REQUEST),
    PROJECT_TITLE_DUPLICATED("1004", "Project title is duplicated: A project with the same title already exists.", HttpStatus.BAD_REQUEST),
    COMPANY_PROJECT_NOT_FOUND("1005", "The company project does not exist for the given project and role.", HttpStatus.NOT_FOUND),
    PROJECT_NOT_FOUND("1006", "Invalid project Id", HttpStatus.NOT_FOUND),
    PROJECT_ALREADY_DELETED("1007", "This Project is already deleted", HttpStatus.NOT_FOUND),
    MEMBER_PROJECT_NOT_FOUND("1008", "The member project does not exist for the given project and role.", HttpStatus.NOT_FOUND),
    STAGE_NOT_FOUND("1009", "The stage does not exist", HttpStatus.NOT_FOUND),
    MEMBER_NOT_IN_PROJECT("1010", "This member does not exist in this project", HttpStatus.NOT_FOUND),
    INVALID_INPUT("1011", "Invalid Input", HttpStatus.BAD_REQUEST),
    INVALID_STAGE_FOR_PROJECT("1012", "Invalid stage for project", HttpStatus.NOT_FOUND),

    INVALID_CREDENTIALS("2001", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("2002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("2003", "잘못된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("2004", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND_MEMBER("2005", "멤버를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_REFRESH_TOKEN("2006", "Refresh token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("2007", "유효하지 않은 Refresh token입니다.", HttpStatus.UNAUTHORIZED),
    DUPLICATE_AUTH_ID("2008", "이미 사용 중인 아이디입니다.", HttpStatus.BAD_REQUEST),
    MAIL_SEND_FAILED("2009", "메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND_COMPANY("2010", "회사를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),


    // 승인요청 관련 오류 메시지
    USER_NOT_IN_PROJECT_DEV("3000", "This user is not in current project or not in project's dev company", HttpStatus.BAD_REQUEST),
    REQUEST_NOT_FOUND("3001", "This request id is not found in Requests", HttpStatus.NOT_FOUND),
    USER_NOT_WRITE_REQUEST("3002", "This user doesn't write this request", HttpStatus.BAD_REQUEST),
    TASK_NOT_FOUND("3003", "This task is not found", HttpStatus.NOT_FOUND),
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