package com.soda.member.service;

import com.soda.common.mail.service.EmailService;
import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.global.security.jwt.JwtTokenProvider;
import com.soda.member.dto.*;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.entity.RefreshToken;
import com.soda.member.repository.CompanyRepository;
import com.soda.member.repository.MemberRepository;
import com.soda.member.repository.RefreshTokenRepository;
import com.soda.member.repository.VerificationCodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private final VerificationCodeRepository verificationCodeRepository;
    private static final long VERIFICATION_CODE_EXPIRATION = 300; // 5분
    private final CompanyRepository companyRepository;

    @Transactional
    public void signup(SignupRequest requestDto) {
        // 아이디 중복 확인
        if (memberRepository.existsByAuthId(requestDto.getAuthId())) {
            throw new GeneralException(ErrorCode.DUPLICATE_AUTH_ID);
        }

        Company company = companyRepository.findByName(requestDto.getCompanyName())
                .orElseThrow(()->new GeneralException(ErrorCode.NOT_FOUND_COMPANY));

        Member member = Member.builder()
                .authId(requestDto.getAuthId())
                .name(requestDto.getName())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole())
                .company(company)
                .position(requestDto.getPosition())
                .phoneNumber(requestDto.getPhoneNumber())
                .build();

        memberRepository.save(member);
    }


    @Transactional
    public LoginResponse login(LoginRequest requestDto, HttpServletRequest request) {
        Member member = memberRepository.findByAuthId(requestDto.getAuthId())
                .orElseThrow(() -> new GeneralException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new GeneralException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getAuthId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAuthId());

        // 기존 리프레시 토큰 삭제 후 새로운 토큰 저장
        refreshTokenRepository.deleteByAuthId(member.getAuthId());

        // 새로운 리프레시 토큰 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .authId(member.getAuthId())
                .token(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResponse refreshAccessToken(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String authId = storedToken.getAuthId();
        String newAccessToken = jwtTokenProvider.createAccessToken(authId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authId);

        // 토큰 초기화
        storedToken.setToken(newRefreshToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void sendVerificationCode(String email) throws IOException {
        String code = generateVerificationCode();
        try {
            emailService.sendVerificationEmail(email, code);
            // Redis 또는 데이터베이스에 인증번호 저장 및 만료 시간 설정
            verificationCodeRepository.saveVerificationCode(email, code, VERIFICATION_CODE_EXPIRATION); // Redis에 인증번호 저장
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.MAIL_SEND_FAILED);
        }
    }


    @Transactional
    public boolean verifyVerificationCode(String email, String code) {
        String storedCode = verificationCodeRepository.findVerificationCode(email);
        if (storedCode != null && storedCode.equals(code)) {
            // 인증 성공 시 Redis에서 인증번호 삭제
            verificationCodeRepository.deleteVerificationCode(email);
            return true;
        }
        return false;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER));

        member.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        memberRepository.save(member);
    }

    // 인증번호 생성 메서드
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }


}