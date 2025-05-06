package com.soda.member.domain;

import com.soda.common.mail.service.EmailService;
import com.soda.global.response.GeneralException;
import com.soda.global.security.jwt.JwtTokenProvider;
import com.soda.member.interfaces.dto.ResetPasswordRequest;
import com.soda.member.interfaces.dto.VerificationConfirmResponse;
import com.soda.member.interfaces.dto.member.LoginRequest;
import com.soda.member.interfaces.dto.member.LoginResponse;
import com.soda.member.interfaces.dto.member.admin.CreateMemberRequest;
import com.soda.member.infrastructure.RefreshTokenRepository;
import com.soda.member.infrastructure.VerificationCodeRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * 회원 가입, 로그인, 토큰 갱신, 이메일 인증, 비밀번호 변경 등의 기능을 제공합니다.
 */
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
    private long refreshTokenValidTimeMillis;

    /** 이메일 인증 코드 유효 시간 */
    private static final Duration VERIFICATION_CODE_EXPIRATION = Duration.ofMinutes(5);
    /** 이메일 인증 코드 길이 */
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 신규 회원을 가입시킵니다.
     * 아이디 및 이메일 중복 검사 후, 비밀번호를 암호화하여 회원 정보를 저장합니다.
     *
     * @param requestDto 회원 가입에 필요한 정보 (아이디, 비밀번호, 이름, 이메일, 회사 ID 등)
     * @throws GeneralException 아이디 또는 이메일 중복 시, 회사 정보를 찾을 수 없을 시 발생
     */
    @Transactional
    public void signup(CreateMemberRequest requestDto) {
        log.info("회원 가입 시도: authId={}", requestDto.getAuthId());
        memberService.validateDuplicateAuthId(requestDto.getAuthId());
        Company company = null;
        if(requestDto.getRole().equals(MemberRole.USER)){
            company = companyService.getCompany(requestDto.getCompanyId());
        }

        Member member = Member.builder()
                .authId(requestDto.getAuthId())
                .name(requestDto.getName())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole())
                .company(company)
                .build();

        Member savedMember = memberService.saveMember(member);
        log.info("회원 가입 성공: authId={}, memberId={}", savedMember.getAuthId(), savedMember.getId());
    }

    /**
     * 사용자 로그인을 처리하고, 성공 시 Access Token과 Refresh Token을 발급합니다.
     * Access Token은 응답 헤더에, Refresh Token은 HttpOnly 쿠키에 담아 전달합니다.
     *
     * @param requestDto 로그인 요청 정보 (아이디, 비밀번호)
     * @param response   HttpServletResponse (헤더 및 쿠키 설정을 위함)
     * @return 로그인한 회원 정보 DTO
     * @throws GeneralException 사용자 정보를 찾을 수 없거나 비밀번호가 일치하지 않을 경우 발생
     */
    @Transactional
    public LoginResponse login(LoginRequest requestDto, HttpServletResponse response) {
        log.info("로그인 시도: authId={}", requestDto.getAuthId());
        Member member = memberService.findMemberByAuthId(requestDto.getAuthId());
        validatePassword(requestDto.getPassword(), member.getPassword(), member.getAuthId());

        if (member.getMemberStatus() == MemberStatus.AWAY || member.getMemberStatus() == null) {
            member.updateMemberStatus(MemberStatus.AVAILABLE);
            log.debug("로그인 성공: 멤버 상태를 AVAILABLE로 변경 (이전 상태: {}) - authId={}",
                    member.getMemberStatus() == null ? "NULL" : "AWAY", member.getAuthId());
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getAuthId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAuthId());
        storeRefreshToken(member.getAuthId(), refreshToken);
        addRefreshTokenCookie(response, refreshToken);
        response.setHeader(jwtTokenProvider.getTokenHeader(), "Bearer " + accessToken);
        log.info("로그인 성공: authId={}", member.getAuthId());
        return LoginResponse.fromEntity(member);
    }

    /**
     * 제공된 Refresh Token을 검증하고, 유효한 경우 새로운 Access Token과 Refresh Token을 발급하여 응답에 설정합니다.
     *
     * @param refreshToken 클라이언트로부터 전달받은 Refresh Token 문자열
     * @param response     HttpServletResponse (갱신된 토큰 설정을 위함)
     * @throws GeneralException Refresh Token이 유효하지 않거나, 만료되었거나, 저장된 토큰과 일치하지 않을 경우 발생
     */
    @Transactional
    public void refreshAccessToken(String refreshToken, HttpServletResponse response) {
        log.debug("Access Token 갱신 시도");

        String authId = null;
        try {
            jwtTokenProvider.validateToken(refreshToken);

            authId = jwtTokenProvider.getAuthId(refreshToken);

            validateStoredRefreshToken(authId, refreshToken);

            String newAccessToken = jwtTokenProvider.createAccessToken(authId);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(authId);

            storeRefreshToken(authId, newRefreshToken);
            addRefreshTokenCookie(response, newRefreshToken);
            response.setHeader(jwtTokenProvider.getTokenHeader(), "Bearer " + newAccessToken);

            log.info("Access Token 갱신 성공: authId={}", authId);

        } catch (ExpiredJwtException e) {
            try { authId = jwtTokenProvider.getAuthId(e.getClaims().getSubject()); } catch (Exception ignored) {}
            log.warn("토큰 갱신 실패: Refresh Token 만료 (authId={})", authId != null ? authId : "추출 불가");
            if (authId != null) refreshTokenRepository.deleteByAuthId(authId);
            throw new GeneralException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);

        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error("토큰 갱신 실패: 유효하지 않은 Refresh Token - {}", e.getMessage());
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);

        } catch (GeneralException e) {
            log.error("토큰 갱신 실패: 저장된 토큰 검증 실패 - {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("토큰 갱신 중 예상치 못한 오류 발생", e);
            throw new GeneralException(AuthErrorCode.TOKEN_REFRESH_FAILED);
        }
    }

    /**
     * 지정된 이메일 주소로 인증 코드를 발송합니다.
     * 이메일 존재 여부를 먼저 확인합니다.
     *
     * @param email 인증 코드를 발송할 이메일 주소
     * @throws GeneralException 해당 이메일의 회원이 존재하지 않거나 메일 발송에 실패한 경우
     */
    @Transactional
    public void sendVerificationCode(String email) {
        log.info("이메일 인증 코드 발송 요청: {}", email);
        memberService.validateEmailExists(email);
        String code = generateVerificationCode();
        sendVerificationEmail(email, code);
        storeVerificationCode(email, code);
        log.info("인증 코드 전송 및 저장 성공: {}", email);
    }

    /**
     * 사용자가 입력한 이메일 인증 코드를 검증합니다.
     * 성공 시 저장된 코드를 삭제하고, 실패 시 예외를 발생시킵니다.
     *
     * @param email 검증할 이메일 주소
     * @param code  사용자가 입력한 인증 코드
     * @return 검증 성공 시, 성공 여부와 이메일 정보가 담긴 DTO
     * @throws GeneralException 저장된 코드가 없거나 만료되었거나, 입력 코드와 일치하지 않을 경우 발생
     */
    @Transactional
    public VerificationConfirmResponse verifyVerificationCode(String email, String code) {
        log.info("이메일 인증 코드 검증 시도: {}", email);
        String storedCode = verificationCodeRepository.findVerificationCode(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodeRepository.deleteVerificationCode(email);
            log.info("인증 코드 검증 성공: {}", email);
            return VerificationConfirmResponse.builder()
                    .isVerified(true)
                    .email(email)
                    .build();
        } else {
            log.warn("인증 코드 검증 실패: 코드 불일치 - {}", email);
            throw new GeneralException(AuthErrorCode.VERIFICATION_CODE_MISMATCH);
        }
    }

    /**
     * 이메일 인증 후 사용자의 비밀번호를 변경합니다.
     * 변경 성공 시, 보안을 위해 해당 사용자의 Refresh Token을 삭제합니다.
     *
     * @param requestDto 비밀번호 변경 요청 정보 (이메일, 새 비밀번호)
     * @throws GeneralException 해당 이메일의 회원을 찾을 수 없는 경우 발생
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest requestDto) {
        log.info("비밀번호 변경 시도: 이메일={}", requestDto.getEmail());
        Member member = memberService.findMemberByEmail(requestDto.getEmail());
        member.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        refreshTokenRepository.deleteByAuthId(member.getAuthId());
        log.info("비밀번호 변경 및 Refresh Token 삭제 완료: 이메일={}", requestDto.getEmail());
    }

    /**
     * 사용자의 로그아웃을 처리합니다.
     * 저장된 Refresh Token을 삭제하고, 클라이언트의 Refresh Token 쿠키를 만료시킵니다.
     *
     * @param response HttpServletResponse (쿠키 삭제를 위함)
     * @throws GeneralException 사용자가 인증되지 않았을 경우 발생
     */
    @Transactional
    public void logout(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("로그아웃 시도: 인증되지 않은 사용자.");
            return;
        }
        String authId = authentication.getName();
        log.info("로그아웃 시도: authId={}", authId);

        try {
            Member member = memberService.findMemberByAuthId(authId);

            member.updateMemberStatus(MemberStatus.AWAY);
            log.debug("멤버 상태를 AWAY로 변경: authId={}", authId);

            refreshTokenRepository.deleteByAuthId(authId);
            log.debug("저장된 Refresh Token 삭제 완료: authId={}", authId);

            clearRefreshTokenCookie(response);

            log.info("로그아웃 성공 및 상태 변경(AWAY) 완료: authId={}", authId);

        } catch (GeneralException e) {
            log.error("로그아웃 처리 중 오류 발생: authId={}", authId, e);
            clearRefreshTokenCookie(response);
            throw e;
        }
    }

    /**
     * 입력된 비밀번호와 저장된 해시 비밀번호를 비교하여 검증합니다.
     *
     * @param inputPassword 사용자 입력 비밀번호
     * @param storedPasswordHash DB에 저장된 암호화된 비밀번호
     * @param authIdForLogging 로그 기록용 사용자 아이디
     * @throws GeneralException 비밀번호가 일치하지 않을 경우
     */
    private void validatePassword(String inputPassword, String storedPasswordHash, String authIdForLogging) {
        if (!passwordEncoder.matches(inputPassword, storedPasswordHash)) {
            log.warn("비밀번호 검증 실패: authId={}", authIdForLogging);
            throw new GeneralException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        log.debug("비밀번호 검증 성공: authId={}", authIdForLogging);
    }

    /**
     * Refresh Token을 저장소에 저장합니다. (기존 토큰 삭제 후 저장)
     *
     * @param authId 사용자 아이디 (토큰의 키로 사용)
     * @param refreshToken 저장할 Refresh Token 문자열
     */
    private void storeRefreshToken(String authId, String refreshToken) {
        refreshTokenRepository.save(authId, refreshToken);
        log.debug("새로운 Refresh Token 저장 완료: authId={}", authId);
    }

    /**
     * 제공된 Refresh Token과 저장소에 저장된 토큰이 일치하는지 검증합니다.
     * 불일치 시 보안 위협으로 간주하고 저장된 토큰을 삭제합니다.
     *
     * @param authId 사용자 아이디
     * @param providedRefreshToken 클라이언트로부터 받은 Refresh Token
     * @throws GeneralException 저장된 토큰이 없거나 제공된 토큰과 일치하지 않을 경우
     */
    private void validateStoredRefreshToken(String authId, String providedRefreshToken) {
        String storedRefreshToken = refreshTokenRepository.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.error("저장된 Refresh Token 검증 실패: 토큰을 찾을 수 없음 - authId={}", authId);
                    return new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (!storedRefreshToken.equals(providedRefreshToken)) {
            log.error("저장된 Refresh Token 검증 실패: 토큰 불일치 - authId={}", authId);
            refreshTokenRepository.deleteByAuthId(authId);
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        log.debug("저장된 Refresh Token 검증 성공: authId={}", authId);
    }

    /**
     * 지정된 이메일로 인증 코드를 발송합니다. (내부 예외 처리)
     *
     * @param email 발송 대상 이메일 주소
     * @param code 발송할 인증 코드
     * @throws GeneralException 이메일 발송 중 오류 발생 시
     */
    private void sendVerificationEmail(String email, String code) {
        try {
            emailService.sendVerificationEmail(email, code);
            log.info("인증 이메일 발송 성공: {}", email);
        } catch (Exception e) {
            log.error("인증 이메일 발송 실패: {}", email, e);
            throw new GeneralException(AuthErrorCode.MAIL_SEND_FAILED);
        }
    }

    /**
     * 생성된 인증 코드를 유효 시간과 함께 저장소(예: Redis)에 저장합니다.
     *
     * @param email 저장할 이메일 (키로 사용)
     * @param code 저장할 인증 코드
     */
    private void storeVerificationCode(String email, String code) {
        verificationCodeRepository.saveVerificationCode(email, code, VERIFICATION_CODE_EXPIRATION.getSeconds());
        log.debug("인증 코드 저장 완료: 이메일={}", email);
    }

    /**
     * 안전한 난수를 이용하여 지정된 길이의 숫자 인증 코드를 생성합니다.
     *
     * @return 생성된 인증 코드 문자열 (예: "123456")
     */
    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Refresh Token을 HttpOnly, Secure 속성을 가진 쿠키로 만들어 응답에 추가합니다.
     *
     * @param response 쿠키를 추가할 HttpServletResponse 객체
     * @param refreshToken 쿠키 값으로 설정할 Refresh Token 문자열
     */
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenValidTimeMillis / 1000));
        response.addCookie(refreshTokenCookie);
        log.debug("Refresh Token 쿠키를 응답에 추가했습니다.");
    }


    /**
     * Refresh Token 쿠키를 삭제(만료)하기 위해 응답에 설정합니다.
     * 쿠키를 생성할 때와 동일한 속성(Path, HttpOnly, Secure)을 사용해야 합니다.
     *
     * @param response 쿠키를 설정할 HttpServletResponse 객체
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
        log.debug("Refresh Token 쿠키를 삭제(만료)하도록 응답에 설정했습니다.");
    }
}