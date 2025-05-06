package com.soda.member.domain;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CompanyErrorCode implements ErrorCode {

    NOT_FOUND_COMPANY("2200", "회사를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_COMPANY_NUMBER("2201", "중복된 사업자 등록번호 입니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_DELETED_COMPANY("2202", "삭제된 회사를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    CompanyErrorCode(String code, String message, HttpStatus httpStatus) {
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