package com.soda.global.security.jwt;

import com.soda.global.response.GeneralException;
import com.soda.member.domain.AuthErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
@Slf4j
@Component
public class JwtTokenProvider {

    private final String secretKeyString;
    private final long accessTokenValidTimeMillis;
    private final long refreshTokenValidTimeMillis;
    private final String header;
    private SecretKey key;

    public JwtTokenProvider(
            @Value("${jwt.secret.key}") String secretKeyString,
            @Value("${jwt.access.expiration}") long accessTokenValidTime,
            @Value("${jwt.refresh.expiration}") long refreshTokenValidTime,
            @Value("${jwt.access.header}") String header) {
        this.secretKeyString = secretKeyString;
        this.accessTokenValidTimeMillis = accessTokenValidTime;
        this.refreshTokenValidTimeMillis = refreshTokenValidTime;
        this.header = header;
    }

    // 빈(Bean) 초기화 시 SecretKey 객체 생성 및 유효성 검사
    @PostConstruct
    protected void init() {
        if (!StringUtils.hasText(secretKeyString)) {
            log.error("!!! 설정 파일에 JWT 비밀 키('jwt.secret.key')가 없거나 비어 있습니다 !!!");
            throw new IllegalArgumentException("JWT 비밀 키('jwt.secret.key')를 설정해야 합니다.");
        }
        try {
            // 비밀 키 문자열을 Base64 디코딩 (일반적인 저장 방식)
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);

            // HS256 알고리즘에 필요한 최소 키 길이 확인 (32바이트 = 256비트)
            if (keyBytes.length < 32) {
                log.warn("!!! JWT 비밀 키('jwt.secret.key')는 HS256 알고리즘 사용 시 최소 256비트(32바이트) 이상을 권장합니다. 현재 길이: {} 바이트 !!!", keyBytes.length);
            }
            this.key = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT 비밀 키 초기화 성공.");
        } catch (IllegalArgumentException e) {
            log.error("!!! JWT 비밀 키('jwt.secret.key') 디코딩 실패. 유효한 Base64 인코딩 문자열인지 확인하세요. 오류: {} !!!", e.getMessage());
            throw new IllegalArgumentException("잘못된 JWT 비밀 키 설정입니다.", e);
        }
    }

    /**
     * 액세스 토큰을 생성합니다.
     * @param authId 사용자의 인증 ID
     * @return 생성된 액세스 토큰 문자열
     */
    public String createAccessToken(String authId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(authId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidTimeMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성합니다.
     * @param authId 사용자의 인증 ID
     * @return 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String authId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(authId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTimeMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * HTTP 요청 헤더에서 토큰을 추출합니다. ('Bearer ' 접두사 제거)
     * @param request HTTP 요청 객체
     * @return 추출된 토큰 문자열 (없거나 형식이 다르면 null 반환)
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        // 'Bearer '로 시작하고 실제 토큰 값이 있는지 확인
        if (bearerToken != null && bearerToken.startsWith("Bearer ") && bearerToken.length() > 7) {
            return bearerToken.substring(7);
        }
        log.debug("요청 헤더 '{}'에서 유효한 Bearer 토큰을 찾을 수 없습니다.", header);
        return null;
    }

    /**
     * 주어진 토큰의 유효성을 검증합니다. (서명, 만료 시간 등)
     * @param token 검증할 토큰 문자열
     * @return 유효하면 true 반환 (단, 예외 발생 시 false 대신 예외를 던짐)
     * @throws IllegalArgumentException 토큰 문자열이 비어있거나 null인 경우
     * @throws ExpiredJwtException 토큰이 만료된 경우
     * @throws UnsupportedJwtException 지원되지 않는 형식의 토큰인 경우
     * @throws MalformedJwtException 토큰 형식이 잘못된 경우
     * @throws SignatureException 서명 검증에 실패한 경우
     * @throws IllegalArgumentException 기타 JWT 관련 인자 오류
     */
    public boolean validateToken(String token) {
        // 토큰 문자열 존재 여부 확인
        if (!StringUtils.hasText(token)) {
            log.warn("토큰 유효성 검증 실패: 토큰 문자열이 비어있거나 null입니다.");
            throw new GeneralException(AuthErrorCode.NOT_FOUND_TOKEN);
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.debug("토큰 유효성 검증 성공.");
            return true;

        } catch (ExpiredJwtException e) {
            log.debug("토큰 유효성 검증 실패: 토큰이 만료되었습니다 - {}", e.getMessage());
            throw e;

        } catch (UnsupportedJwtException e) {
            log.warn("토큰 유효성 검증 실패: 지원되지 않는 JWT 토큰입니다 - {}", e.getMessage());
            throw e;

        } catch (MalformedJwtException e) {
            log.warn("토큰 유효성 검증 실패: 잘못된 JWT 토큰 형식입니다 - {}", e.getMessage());
            throw e;

        } catch (SignatureException e) {
            log.warn("토큰 유효성 검증 실패: 잘못된 JWT 서명입니다 - {}", e.getMessage());
            throw e;

        } catch (IllegalArgumentException e) {
            log.warn("토큰 유효성 검증 실패: JWT 인자 검증 오류 - {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 토큰에서 Claims 정보를 추출합니다. (만료 여부 등은 검증됨)
     * @param token Claims를 추출할 토큰 문자열
     * @return 추출된 Claims 객체
     * @throws IllegalArgumentException 토큰 문자열이 비어있거나 null인 경우
     * @throws ExpiredJwtException 토큰이 만료된 경우 (일반적으로 유효하지 않음)
     * @throws JwtException 유효하지 않은 토큰 (서명, 형식 등 오류)
     */
    public Claims getClaims(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Claims를 가져올 수 없음: 토큰 문자열이 비어있거나 null입니다.");
            throw new IllegalArgumentException("JWT 토큰 문자열이 비어있거나 null입니다.");
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서 Claims를 읽어야 할 특별한 경우가 아니라면 예외 발생시킴
            log.debug("Claims를 가져올 수 없음: 토큰이 만료되었습니다 - {}", e.getMessage());
            throw e;
        } catch (JwtException e) { // Malformed, Signature, Unsupported 등을 포함한 JWT 관련 예외
            log.warn("Claims를 가져올 수 없음: 유효하지 않은 JWT입니다 - {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 토큰에서 사용자 인증 ID (Subject)를 추출합니다.
     * @param token 인증 ID를 추출할 토큰 문자열
     * @return 추출된 사용자 인증 ID
     * @throws JwtException 토큰이 유효하지 않은 경우 (getClaims 내부에서 발생)
     */
    public String getAuthId(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (JwtException e) {
            log.error("AuthId 추출 실패: 유효하지 않은 토큰으로 Claims 획득 불가 - {}", e.getMessage());
            throw new GeneralException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public String getTokenHeader() {
        return header;
    }
}