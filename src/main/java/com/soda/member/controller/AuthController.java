package com.soda.member.controller;

import com.soda.common.mail.dto.EmailRequest;
import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.*;
import com.soda.member.dto.member.LoginRequest;
import com.soda.member.dto.member.LoginResponse;
import com.soda.member.dto.member.admin.CreateMemberRequest;
import com.soda.member.service.AuthService;
import com.soda.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseForm<Void>> signup(@RequestBody CreateMemberRequest requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회원가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseForm<LoginResponse>> login(@RequestBody LoginRequest requestDto, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(requestDto, response);
        return ResponseEntity.ok(ApiResponseForm.success(loginResponse, "로그인 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseForm<Void>> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        authService.refreshAccessToken(refreshToken, response);
        return ResponseEntity.ok(ApiResponseForm.success(null, "액세스 토큰 재발급 성공"));
    }

    @PostMapping("/verification")
    public ResponseEntity<ApiResponseForm> sendVerificationCode(@RequestBody EmailRequest request) throws IOException {
        authService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponseForm.success(null));
    }

    @PostMapping("/verification/confirm")
    public ResponseEntity<ApiResponseForm<VerificationConfirmResponse>> verifyVerificationCode(@RequestBody EmailVerificationRequest request) {
        VerificationConfirmResponse verificationConfirmResponse = authService.verifyVerificationCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponseForm.success(verificationConfirmResponse, "인증번호 확인 결과"));
    }

    @PostMapping("/password/change")
    public ResponseEntity<ApiResponseForm<Void>> changePassword(@RequestBody ChangePasswordRequest requestDto) {
        authService.changePassword(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "비밀번호 변경 성공"));
    }

}