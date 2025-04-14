package com.soda.global.response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("[Error Log] requestUrl: {}, requestMethod: {}, userId: {}, clientIp: {}, exception: {}, message: {}, responseStatus: {}",
                request.getRequestURI(), request.getMethod(), (request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : "Anonymous", request.getRemoteAddr(), "HttpMessageNotReadableException", "요청 형식이 올바르지 않습니다.", 400);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseForm.error("400", "요청 형식이 올바르지 않습니다."));
    }


    // --- @Valid 유효성 검사 실패 핸들러 추가 ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseForm<Void>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult bindingResult = ex.getBindingResult();

        String errorMessage = "입력 값 유효성 검사 실패";
        if (bindingResult.hasFieldErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                errorMessage = fieldError.getDefaultMessage();
            }
        }

        String errorCode = "400";

        log.warn("[Warning Log] requestUrl: {}, requestMethod: {}, userId: {}, clientIp: {}, exception: {}, message: {}, responseStatus: {}",
                request.getRequestURI(), request.getMethod(), (request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : "Anonymous", request.getRemoteAddr(), ex.getClass().getSimpleName(), errorMessage, HttpStatus.BAD_REQUEST.value()); // 로그 레벨 Warn으로 조정

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseForm.error(errorCode, errorMessage));
    }
}