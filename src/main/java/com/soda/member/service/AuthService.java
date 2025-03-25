package com.soda.member.service;

import com.soda.common.mail.service.EmailService;
import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.global.security.jwt.JwtTokenProvider;
import com.soda.member.dto.*;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.repository.CompanyRepository;
import com.soda.member.repository.MemberRepository;
import com.soda.member.repository.RefreshTokenRepository;
import com.soda.member.repository.VerificationCodeRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidTime;

    private static final long VERIFICATION_CODE_EXPIRATION = 300; // 5분
    private final CompanyRepository companyRepository;

    @Transactional
    public void signup(SignupRequest requestDto) {
        // 아이디 중복 확인
        if (memberRepository.existsByAuthId(requestDto.getAuthId())) {
            throw new GeneralException(ErrorCode.DUPLICATE_AUTH_ID);
        }

        Company company = companyRepository.findByName(requestDto.getCompanyName())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_COMPANY));

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
    public void login(LoginRequest requestDto, HttpServletResponse response) {
        Member member = memberRepository.findByAuthId(requestDto.getAuthId())
                .orElseThrow(() -> new GeneralException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new GeneralException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getAuthId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAuthId());

        // 기존 리프레시 토큰 삭제 후 새로운 토큰 저장
        refreshTokenRepository.deleteByAuthId(member.getAuthId());
        refreshTokenRepository.save(member.getAuthId(), refreshToken);

        // 리프레시 토큰을 HttpOnly 쿠키에 저장
        addRefreshTokenCookie(response, refreshToken);

        // 액세스 토큰을 Authorization 헤더에 담아 응답
        response.setHeader("Authorization", "Bearer " + accessToken);


    }

    @Transactional
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {


        String refreshToken = jwtTokenProvider.resolveToken(request);
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String AuthId = jwtTokenProvider.getAuthId(refreshToken);

        String storedRefreshToken = refreshTokenRepository.findByAuthId(AuthId).orElseThrow(() ->
                new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedRefreshToken.equals(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(AuthId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(AuthId);

        // 기존 리프레시 토큰 삭제 후 새로운 토큰 저장
        refreshTokenRepository.deleteByAuthId(AuthId);
        refreshTokenRepository.save(AuthId, newRefreshToken);

        // 리프레시 토큰을 HttpOnly 쿠키에 저장
        addRefreshTokenCookie(response, refreshToken);

        // 새 액세스 토큰을 Authorization 헤더에 설정
        response.setHeader("Authorization", "Bearer " + newAccessToken);
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

    @Transactional
    public void updateMember(Long memberId, UpdateMemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_MEMBER));
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_COMPANY));

//        member.updateMember(request,company);
    }

    // 인증번호 생성 메서드
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // refreshToken을 쿠키에 넣는 메서드
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenValidTime / 1000));
        response.addCookie(refreshTokenCookie);
    }

}