package com.soda.member.interfaces;

import com.soda.member.interfaces.dto.mail.EmailRequest;
import com.soda.global.response.ApiResponseForm;
import com.soda.member.interfaces.dto.EmailVerificationRequest;
import com.soda.member.interfaces.dto.ResetPasswordRequest;
import com.soda.member.interfaces.dto.VerificationConfirmResponse;
import com.soda.member.interfaces.dto.member.LoginRequest;
import com.soda.member.interfaces.dto.member.LoginResponse;
import com.soda.member.interfaces.dto.member.admin.CreateMemberRequest;
import com.soda.member.domain.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponseForm<Void>> resetPassword(@RequestBody ResetPasswordRequest requestDto) {
        authService.resetPassword(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "비밀번호 재설정 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseForm<Void>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(ApiResponseForm.success(null, "로그아웃 성공"));
    }

    @GetMapping("/check-id")
    public ResponseEntity<ApiResponseForm<Boolean>> checkIdAvailability(@RequestParam String authId) {
        boolean isAvailable = authService.checkIdAvailability(authId);
        return ResponseEntity.ok(ApiResponseForm.success(isAvailable, "아이디 중복 확인 성공"));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponseForm<Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(ApiResponseForm.success(isAvailable, "이메일 중복 확인 성공"));
    }

    @GetMapping("/system-check/headers")
    public ResponseEntity<Map<String, String>> getHeadersAndScheme(HttpServletRequest request) {
        log.info("===== [DEBUG] Request Headers Check Start =====");
        Map<String, String> result = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            result.put(headerName, headerValue);
            log.info("Header: {} = {}", headerName, headerValue);
        }

        String scheme = request.getScheme();
        result.put("X-REQUEST-SCHEME", scheme);
        log.info(">>>>>> Final Scheme Detected: {}", scheme);

        log.info("===== [DEBUG] Request Headers Check End =====");

        return ResponseEntity.ok(result);
    }

}