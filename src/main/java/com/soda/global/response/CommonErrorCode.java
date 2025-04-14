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

    // 공통 에러
    UNEXPECTED_ERROR("1000", "Unexpected Error: An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),
    S3_UPLOAD_ERROR("1001", "S3 Upload Error: An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 승인요청 관련 오류 메시지
    USER_NOT_IN_PROJECT_DEV("3000", "This user is not in current project or not in project's dev company", HttpStatus.BAD_REQUEST),
    USER_NOT_IN_PROJECT_CLI("3004", "This user is not in current project or not in project's cli company, or not an admin", HttpStatus.NOT_FOUND ),
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