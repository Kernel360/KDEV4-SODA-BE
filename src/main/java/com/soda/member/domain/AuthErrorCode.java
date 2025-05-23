package com.soda.member.domain;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS("2001", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("2002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("2003", "잘못된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("2004", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND_ACCESS_TOKEN("2005", "Access Token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_REFRESH_TOKEN("2006", "Refresh Token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_TOKEN("2007", "Token을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("2008", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
    MAIL_SEND_FAILED("2009", "메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTHENTICATION_FAILED("2010", "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_EMAIL("2011", "가입되지 않은 이메일 입니다.", HttpStatus.UNAUTHORIZED),
    VERIFICATION_CODE_MISMATCH("2012" ,"인증번호가 일치하지 않거나 유효하지 않습니다.",HttpStatus.BAD_REQUEST),
    EXPIRED_REFRESH_TOKEN("2013","Refresh Token이 만료되었습니다." ,HttpStatus.UNAUTHORIZED ),
    TOKEN_REFRESH_FAILED("2014","Access Token 재발급을 실패했습니다." ,HttpStatus.BAD_REQUEST );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(String code, String message, HttpStatus httpStatus) {
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
