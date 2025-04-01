package com.soda.member.service;

import com.soda.common.mail.service.EmailService;
import com.soda.global.response.GeneralException;
import com.soda.global.security.jwt.JwtTokenProvider;
import com.soda.member.dto.ChangePasswordRequest;
import com.soda.member.dto.LoginRequest;
import com.soda.member.dto.MemberUpdateRequest;
import com.soda.member.dto.SignupRequest;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.error.AuthErrorCode;
import com.soda.member.error.CompanyErrorCode;
import com.soda.member.error.MemberErrorCode;
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

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CompanyService companyService;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidTime;

    private static final long VERIFICATION_CODE_EXPIRATION = 300; // 5분

    @Transactional
    public void signup(SignupRequest requestDto) {
        memberService.validateDuplicateAuthId(requestDto.getAuthId());
        Company company = companyService.getCompany(requestDto.getCompanId());
        Member member = memberService.createMember(requestDto, company, passwordEncoder);
        memberService.saveMember(member);
        log.info("회원 가입 성공: {}", member.getAuthId());
    }

    @Transactional
    public void login(LoginRequest requestDto, HttpServletResponse response) {
        Member member = memberService.findMemberByAuthId(requestDto.getAuthId());
        validatePassword(requestDto.getPassword(), member.getPassword(), requestDto.getAuthId());
        String accessToken = jwtTokenProvider.createAccessToken(member.getAuthId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAuthId());
        storeRefreshToken(member.getAuthId(), refreshToken);
        addRefreshTokenCookie(response, refreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);
        log.info("로그인 성공: {}", member.getAuthId());
    }

    @Transactional
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveToken(request);

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.error("토큰 갱신 실패: 유효하지 않은 리프레시 토큰");
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String authId = jwtTokenProvider.getAuthId(refreshToken);
        String storedRefreshToken = refreshTokenRepository.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.error("토큰 갱신 실패: 리프레시 토큰을 찾을 수 없음 - {}", authId);
                    return new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (!storedRefreshToken.equals(refreshToken)) {
            log.error("토큰 갱신 실패: 리프레시 토큰 불일치 - {}", authId);
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(authId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authId);

        refreshTokenRepository.deleteByAuthId(authId);
        refreshTokenRepository.save(authId, newRefreshToken);

        addRefreshTokenCookie(response, newRefreshToken); // 수정된 부분
        response.setHeader("Authorization", "Bearer " + newAccessToken);

        log.info("토큰 갱신 성공: {}", authId);
    }

    @Transactional
    public void sendVerificationCode(String email) throws IOException {
        String code = generateVerificationCode();
        try {
            emailService.sendVerificationEmail(email, code);
            verificationCodeRepository.saveVerificationCode(email, code, VERIFICATION_CODE_EXPIRATION);
            log.info("인증 코드 전송 성공: {}", email);
        } catch (Exception e) {
            log.error("인증 코드 전송 실패: {}", email, e);
            throw new GeneralException(AuthErrorCode.MAIL_SEND_FAILED);
        }
    }

    @Transactional
    public boolean verifyVerificationCode(String email, String code) {
        String storedCode = verificationCodeRepository.findVerificationCode(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodeRepository.deleteVerificationCode(email);
            log.info("인증 코드 검증 성공: {}", email);
            return true;
        }
        log.warn("인증 코드 검증 실패: {}", email);
        return false;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest requestDto) {
        Member member = memberService.findMemberByEmail(requestDto.getEmail());
        memberService.updateMemberPassword(member, passwordEncoder.encode(requestDto.getNewPassword()));
    }

    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberService.findMemberById(memberId);
        Company company = companyService.getCompany(request.getCompanyId());
        memberService.updateMember(member, request, company);
    }

    private void validatePassword(String inputPassword, String storedPassword, String authId) {
        if (!passwordEncoder.matches(inputPassword, storedPassword)) {
            log.error("로그인 실패: 잘못된 아이디 또는 비밀번호 - {}", authId);
            throw new GeneralException(AuthErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void storeRefreshToken(String authId, String refreshToken) {
        refreshTokenRepository.deleteByAuthId(authId);
        refreshTokenRepository.save(authId, refreshToken);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenValidTime / 1000));
        response.addCookie(refreshTokenCookie);
    }
}