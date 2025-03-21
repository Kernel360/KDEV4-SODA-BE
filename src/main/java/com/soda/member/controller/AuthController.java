package com.soda.member.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.member.dto.LoginRequestDto;
import com.soda.member.dto.LoginResponseDto;
import com.soda.member.dto.SignupRequestDto;
import com.soda.member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseForm<Void>> signup(@RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(ApiResponseForm.success(null, "회원가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseForm<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto, HttpServletRequest request) {
        LoginResponseDto token = authService.login(requestDto,request);
        return ResponseEntity.ok(ApiResponseForm.success(token, "로그인 성공"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseForm<LoginResponseDto>> refresh(HttpServletRequest request) {
        LoginResponseDto responseDto = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponseForm.success(responseDto, "액세스 토큰 재발급 성공"));
    }
}