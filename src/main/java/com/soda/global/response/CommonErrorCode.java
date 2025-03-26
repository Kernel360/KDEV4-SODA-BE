package com.soda.global.response;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

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
    DUPLICATE_COMPANY_NUMBER("2011", "이미 존재하는 회사입니다.", HttpStatus.BAD_REQUEST),

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
    CommonErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    // 코드와 메시지를 반환하는 메서드
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