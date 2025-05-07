package com.soda.member.domain;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements ErrorCode {
    NOT_FOUND_MEMBER("2301", "회원을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_ADMIN("2302", "관리자 권한이 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_AUTHORIZED("2303", "접근 권한이 없습니다.", HttpStatus.BAD_REQUEST),
    DELETED_MEMBER("2304", "삭제된 회원입니다.", HttpStatus.BAD_REQUEST),
    INACTIVE_MEMBER("2305", "비활성화된 회원입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL("2306", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_AUTH_ID("2307", "이미 사용 중인 아이디입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("2308", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT("2309", "올바른 이메일 형식이 아닙니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT("2310", "올바른 비밀번호 형식이 아닙니다.", HttpStatus.BAD_REQUEST),
    CANNOT_DEACTIVATE_SELF("2311", "본인 계정은 비활성화 할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_CURRENT_PASSWORD("2312", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD("2313","새 비밀번호는 현재 비밀번호와 달라야 합니다." , HttpStatus.BAD_REQUEST);

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
