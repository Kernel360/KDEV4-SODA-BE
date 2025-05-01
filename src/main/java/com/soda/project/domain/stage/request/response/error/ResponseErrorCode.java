package com.soda.project.domain.stage.request.response.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ResponseErrorCode implements ErrorCode {
    RESPONSE_NOT_FOUND("3101", "This response id is not found in Requests", HttpStatus.NOT_FOUND),
    USER_NOT_WRITE_RESPONSE("3102", "This user didn't write the response", HttpStatus.UNAUTHORIZED),
    RESPONSE_FILE_NOT_FOUND("3103", "This response_file is not found" , HttpStatus.NOT_FOUND),
    USER_NOT_UPLOAD_FILE("3104", "This user didn't upload the response_file" , HttpStatus.UNAUTHORIZED ),
    RESPONSE_LINK_NOT_FOUND("3105", "This response link is not found" , HttpStatus.NOT_FOUND ),
    USER_NOT_UPLOAD_LINK("3106", "This user didn't upload the response_link" , HttpStatus.UNAUTHORIZED ),;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ResponseErrorCode (String code, String message, HttpStatus httpStatus) {
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
