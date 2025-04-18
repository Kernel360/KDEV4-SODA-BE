package com.soda.request.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RequestErrorCode implements ErrorCode {
    REQUEST_NOT_FOUND("3001", "This request id is not found in Requests", HttpStatus.NOT_FOUND),
    USER_NOT_WRITE_REQUEST("3002", "This user doesn't write this request", HttpStatus.BAD_REQUEST),
    REQUEST_FILE_NOT_FOUND("3003", "This request_file is not found", HttpStatus.NOT_FOUND),
    USER_NOT_UPLOAD_FILE("3004", "This user didn't upload the request_file", HttpStatus.BAD_REQUEST),
    REQUEST_LINK_NOT_FOUND("3005", "This request_link is not found" , HttpStatus.NOT_FOUND ),
    USER_NOT_UPLOAD_LINK("3006", "This user didn't upload the request_link" , HttpStatus.BAD_REQUEST ),
    USER_IS_NOT_APPROVER("3007", "This user is not approver", HttpStatus.BAD_REQUEST ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    RequestErrorCode (String code, String message, HttpStatus httpStatus) {
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
