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

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CompanyRepository companyRepository;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidTime;

    private static final long VERIFICATION_CODE_EXPIRATION = 300; // 5분

    @Transactional
    public void signup(SignupRequest requestDto) {
        if (memberRepository.existsByAuthId(requestDto.getAuthId())) {
            log.error("회원 가입 실패: 아이디 중복 - {}", requestDto.getAuthId());
            throw new GeneralException(MemberErrorCode.DUPLICATE_AUTH_ID);
        }

        Company company = companyRepository.findByName(requestDto.getCompanyName())
                .orElseThrow(() -> {
                    log.error("회원 가입 실패: 회사를 찾을 수 없음 - {}", requestDto.getCompanyName());
                    return new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY);
                });

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
        log.info("회원 가입 성공: {}", member.getAuthId());
    }

    @Transactional
    public void login(LoginRequest requestDto, HttpServletResponse response) {
        Member member = memberRepository.findByAuthId(requestDto.getAuthId())
                .orElseThrow(() -> {
                    log.error("로그인 실패: 잘못된 아이디 또는 비밀번호 - {}", requestDto.getAuthId());
                    return new GeneralException(AuthErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            log.error("로그인 실패: 잘못된 아이디 또는 비밀번호 - {}", requestDto.getAuthId());
            throw new GeneralException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getAuthId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAuthId());

        refreshTokenRepository.deleteByAuthId(member.getAuthId());
        refreshTokenRepository.save(member.getAuthId(), refreshToken);

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
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> {
                    log.error("비밀번호 변경 실패: 멤버를 찾을 수 없음 - {}", requestDto.getEmail());
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });

        member.changePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        memberRepository.save(member);
        log.info("비밀번호 변경 성공: {}", member.getAuthId());
    }

    @Transactional
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("멤버 정보 수정 실패: 멤버를 찾을 수 없음 - {}", memberId);
                    return new GeneralException(MemberErrorCode.NOT_FOUND_MEMBER);
                });

        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseThrow(() -> {
                    log.error("멤버 정보 수정 실패: 회사를 찾을 수 없음 - {}", request.getCompanyName());
                    return new GeneralException(CompanyErrorCode.NOT_FOUND_COMPANY);
                });

        member.updateMember(request, company);
        memberRepository.save(member);
        log.info("멤버 정보 수정 성공: {}", memberId);
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