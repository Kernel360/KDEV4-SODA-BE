package com.soda.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.global.response.ApiResponseForm;
import com.soda.global.response.ErrorCode;
import com.soda.member.error.AuthErrorCode;
import com.soda.member.error.MemberErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * JWT 관련 예외 및 인증 과정 중 발생하는 특정 예외를 처리하는 필터.
 * JwtAuthenticationFilter 앞에 위치하여 해당 필터 또는 그 이후 과정에서 발생하는 예외를 가로채서
 * 표준화된 오류 응답 형식을 클라이언트에게 반환합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug(">>> JwtExceptionFilter 시작 - URI: {}", request.getRequestURI());
        try {
            // 다음 필터(JwtAuthenticationFilter)로 요청 전달
            filterChain.doFilter(request, response);
            // 다음 필터들이 예외 없이 정상적으로 완료된 경우 로그 (디버그 레벨)
            log.debug(">>> JwtExceptionFilter - 필터 체인 정상 완료됨 (특정 예외 없음).");

        } catch (ExpiredJwtException e) {
            // JWT 만료 예외 처리
            log.warn(">>> 예외 필터: 토큰 만료 감지됨 - {}", e.getMessage());
            setErrorResponse(response, AuthErrorCode.TOKEN_EXPIRED);

        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            // JWT 형식 오류, 서명 오류, 지원되지 않는 토큰 예외 처리
            log.warn(">>> 예외 필터: 유효하지 않은 토큰 감지됨 (형식/서명/지원) - {}", e.getMessage());
            setErrorResponse(response, AuthErrorCode.INVALID_TOKEN);

        } catch (IllegalArgumentException e) {
            // JWT 처리 중 발생할 수 있는 IllegalArgumentException 처리 (예: 빈 토큰 문자열)
            log.warn(">>> 예외 필터: 잘못된 인자 또는 빈 토큰 감지됨 - {}", e.getMessage());
            setErrorResponse(response, AuthErrorCode.INVALID_TOKEN);

        } catch (BadCredentialsException e) {
            // JwtAuthenticationFilter에서 토큰이 없을 때 던진 예외 포함 가능성
            log.warn(">>> 예외 필터: 잘못된 자격 증명 또는 토큰 없음 감지됨 - {}", e.getMessage());
            if ("인증 토큰이 필요합니다.".equals(e.getMessage())) {
                // JwtAuthenticationFilter에서 토큰 없음을 이유로 던진 경우
                setErrorResponse(response, AuthErrorCode.NOT_FOUND_ACCESS_TOKEN);
            } else {
                // 다른 이유로 발생한 BadCredentialsException (가능성은 낮지만)
                setErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
            }
        } catch (UsernameNotFoundException e) {
            // UserDetailsService에서 사용자를 찾지 못했을 때 발생하는 예외 처리
            log.warn(">>> 예외 필터: 사용자를 찾을 수 없음 감지됨 - {}", e.getMessage());
            setErrorResponse(response, MemberErrorCode.NOT_FOUND_MEMBER);

        } catch (JwtException e) {
            // 위에서 명시적으로 잡지 못한 기타 JWT 관련 예외 처리 (포괄적)
            log.warn(">>> 예외 필터: 기타 JWT 관련 오류 감지됨 - {}", e.getMessage());
            setErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
        }
        // 여기서 다른 RuntimeException을 잡지 않음으로써, 예상치 못한 서버 내부 오류는
        // 스프링 부트의 기본 500 에러 처리나 @ControllerAdvice로 넘어갑니다.

        log.debug(">>> JwtExceptionFilter 종료 - URI: {}", request.getRequestURI());
    }

    /**
     * 표준화된 오류 응답을 생성하여 HttpServletResponse에 작성합니다.
     * @param response HttpServletResponse 객체
     * @param errorCode 사용할 오류 코드 (상태 코드, 코드 문자열, 메시지 포함)
     * @throws IOException 응답 작성 중 I/O 오류 발생 시
     */
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        // 오류 응답 설정 시작 로그 (디버그 레벨)
        log.debug(">>> 오류 응답 설정 시작. ErrorCode: {}", errorCode);

        // 응답이 이미 커밋되었는지 확인
        if (response.isCommitted()) {
            log.warn("응답이 이미 커밋되어 오류 응답을 보낼 수 없습니다 (예외 필터). ErrorCode: {}", errorCode);
            return;
        }

        // HTTP 상태 코드, Content Type, 문자 인코딩 설정
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        log.debug(">>> 오류 응답 설정 - 상태 코드 및 헤더 설정 완료.");

        try {
            // 응답 본문 생성 (ApiResponseForm 사용)
            ApiResponseForm<Void> errorResponse = ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage());
            log.debug(">>> 오류 응답 설정 - ApiResponseForm 생성 완료.");

            // JSON 문자열로 변환
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            log.debug(">>> 오류 응답 설정 - JSON 변환 완료: {}", jsonResponse);

            // 응답 스트림에 JSON 작성
            OutputStream out = response.getOutputStream();
            out.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            log.debug(">>> 오류 응답 설정 - 응답 본문 작성 완료.");

            // 응답 스트림 flush (실제 전송)
            out.flush();
            log.debug(">>> 오류 응답 설정 - 응답 flushed.");

        } catch (IOException e) {
            log.error(">>> 오류 응답 설정 - 응답 작성/전송 중 IOException 발생! ErrorCode: {}", errorCode, e);
            throw e;
        } catch (Exception e) {
            // JSON 변환 오류 등 기타 예상치 못한 오류 발생 시
            log.error(">>> 오류 응답 설정 - 예상치 못한 내부 오류 발생! ErrorCode: {}", errorCode, e);
            // 클라이언트에게 정상 응답을 줄 수 없으므로 RuntimeException 발생시켜 500 에러 유도
            throw new RuntimeException("JwtExceptionFilter에서 오류 응답 생성/전송 중 오류 발생", e);
        }
        log.debug(">>> 오류 응답 설정 종료.");
    }
}