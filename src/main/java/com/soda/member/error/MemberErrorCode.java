package com.soda.member.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements ErrorCode {
    NOT_FOUND_MEMBER("2301", "멤버를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_AUTH_ID("2302", "이미 사용 중인 아이디입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_ADMIN("2303", "관리자가 아닙니다", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    MemberErrorCode (String code, String message, HttpStatus httpStatus) {
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
