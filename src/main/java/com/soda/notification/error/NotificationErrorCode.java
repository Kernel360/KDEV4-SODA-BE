package com.soda.notification.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum NotificationErrorCode implements ErrorCode { // <<< 클래스명 수정

    USER_INFO_NOT_FOUND("2401", "SSE 구독 요청 - 사용자 인증 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    USER_ID_NULL("2402", "SSE 구독 요청 - User ID가 null입니다.", HttpStatus.BAD_REQUEST),
    USER_ID_PARSE_FAILED("2403", "SSE 구독 요청 - 사용자 ID 추출에 실패했습니다.", HttpStatus.BAD_REQUEST),

    SSE_CONNECTION_ERROR("2404", "SSE 구독 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_NOT_FOUND("2405", "해당 알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FORBIDDEN_ACCESS_NOTIFICATION("2406", "해당 알림에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    NotificationErrorCode(String code, String message, HttpStatus httpStatus) {
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
