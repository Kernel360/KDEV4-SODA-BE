package com.soda.member.controller;

import com.soda.common.mail.dto.EmailRequest;
import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.*;
import com.soda.member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseForm<Void>> signup(@RequestBody SignupRequest requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회원가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseForm<LoginResponse>> login(@RequestBody LoginRequest requestDto, HttpServletRequest request) {
        LoginResponse token = authService.login(requestDto, request);
        return ResponseEntity.ok(ApiResponseForm.success(token, "로그인 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseForm<LoginResponse>> refresh(HttpServletRequest request) {
        LoginResponse responseDto = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponseForm.success(responseDto, "액세스 토큰 재발급 성공"));
    }

    @PostMapping("/verification")
    public ResponseEntity<ApiResponseForm> sendVerificationCode(@RequestBody EmailRequest request) throws IOException {
        authService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @PostMapping("/verification/confirm")
    public ResponseEntity<ApiResponseForm<Boolean>> verifyVerificationCode(@RequestBody EmailVerificationRequest request) {
        boolean isVerified = authService.verifyVerificationCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponseForm.success(isVerified, "인증번호 확인 결과"));
    }

    @PostMapping("/password/change")
    public ResponseEntity<ApiResponseForm<Void>> changePassword(@RequestBody ChangePasswordRequest requestDto) {
        authService.changePassword(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "비밀번호 변경 성공"));
    }

}