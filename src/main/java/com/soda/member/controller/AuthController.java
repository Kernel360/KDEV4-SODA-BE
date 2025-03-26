package com.soda.member.controller;

import com.soda.common.mail.dto.EmailRequest;
import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.*;
import com.soda.member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<ApiResponseForm<Void>> login(@RequestBody LoginRequest requestDto, HttpServletResponse response) {
        authService.login(requestDto, response);
        return ResponseEntity.ok(ApiResponseForm.success(null, "로그인 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseForm<Void>> refresh(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshAccessToken(request, response);
        return ResponseEntity.ok(ApiResponseForm.success(null, "액세스 토큰 재발급 성공"));
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

    @PutMapping("/members/{memberId}")
    public ResponseEntity<ApiResponseForm<Void>> updateMember(@PathVariable Long memberId, @RequestBody MemberUpdateRequest request) {
        authService.updateMember(memberId, request);
        return ResponseEntity.ok(ApiResponseForm.success(null, "멤버 정보 수정 성공"));
    }
}