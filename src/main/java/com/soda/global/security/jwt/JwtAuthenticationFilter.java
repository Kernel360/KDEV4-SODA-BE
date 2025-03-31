package com.soda.global.security.jwt;

import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.global.security.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * HTTP 요청 헤더에서 JWT 토큰을 추출하고 유효성을 검사하여,
 * 유효한 경우 SecurityContextHolder에 인증(Authentication) 객체를 설정하는 필터입니다.
 * 토큰 관련 예외 처리는 이 필터 앞단의 JwtExceptionFilter에서 담당합니다.
 * 단, 인증이 필요한 경로에 토큰 없이 접근 시도 시 BadCredentialsException을 발생시킵니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성, 검증, 정보 추출 담당
    private final UserDetailsService userDetailsService; // 사용자 정보 로드 서비스
    private final SecurityProperties securityProperties; // 시큐리티 관련 설정값 (제외 경로 등)
    private final AntPathMatcher antPathMatcher = new AntPathMatcher(); // URL 경로 매칭 유틸리티

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 제외 경로 확인: 요청 경로가 필터 적용 제외 대상인지 확인합니다.
        if (isExcludedPath(request.getServletPath())) {
            log.debug("제외 경로 요청이므로 필터를 건너갑니다: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 토큰 추출: HTTP 요청 헤더에서 JWT 토큰을 가져옵니다.
        String token = jwtTokenProvider.resolveToken(request);

        // 3. 토큰 유무에 따른 처리
        if (StringUtils.hasText(token)) {
            // 3-1. 토큰이 있는 경우: 인증 절차 수행
            //      (validateToken, getClaims, loadUserByUsername 등에서 예외 발생 시,
            //       이 필터는 잡지 않고 그대로 전파하여 앞단의 JwtExceptionFilter가 처리하도록 함)
            log.debug("JWT 토큰 발견됨. 인증 절차를 시작합니다...");

            // 토큰 유효성 검증 (만료, 서명 등). 실패 시 예외 발생.
            jwtTokenProvider.validateToken(token);
            // 토큰에서 Claims 정보 추출. 실패 시 예외 발생.
            Claims claims = jwtTokenProvider.getClaims(token);
            // Claims에서 사용자 인증 ID 추출
            String authId = claims.getSubject();

            // 사용자 인증 ID를 이용하여 UserDetails 객체 로드. 사용자를 못 찾으면 예외 발생.
            UserDetails userDetails = userDetailsService.loadUserByUsername(authId);

            // 인증(Authentication) 객체 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            // SecurityContextHolder에 인증 객체 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JWT 인증 성공. 사용자: {}", authId);

            // 부가 정보(memberId 등)를 request attribute에 추가 (컨트롤러 등에서 사용 가능)
            addUserAttributes(request, userDetails);

        } else {
            // 3-2. 토큰이 없는 경우:
            //      제외 경로가 아니므로 인증이 필요한 경로로 간주합니다.
            //      인증 토큰이 없다는 것은 인증 실패 상황이므로 예외를 발생시켜
            //      앞단의 JwtExceptionFilter가 401 응답을 보내도록 합니다.
            log.warn("헤더에 JWT 토큰이 없습니다. 인증이 필요한 경로일 수 있습니다. URI: {}", request.getRequestURI());
            // BadCredentialsException 발생 (JwtExceptionFilter에서 이 메시지로 구분 가능)
            throw new BadCredentialsException("인증 토큰이 필요합니다.");
        }

        // 4. 다음 필터로 진행
        //    (정상 처리되었거나, 또는 예외가 발생하여 JwtExceptionFilter로 제어가 넘어갔을 것)
        filterChain.doFilter(request, response);
    }

    /**
     * 인증된 사용자의 추가 정보(memberId, role 등)를 HttpServletRequest의 attribute에 설정합니다.
     * 컨트롤러 등 후속 처리 단계에서 이 정보를 활용할 수 있습니다.
     * @param request HttpServletRequest 객체
     * @param userDetails 인증된 사용자의 UserDetails 객체
     */
    private void addUserAttributes(HttpServletRequest request, UserDetails userDetails) {
        // UserDetails 객체가 우리가 정의한 UserDetailsImpl 타입인지 확인
        if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
            // UserDetailsImpl 타입이면 Member 엔티티에서 추가 정보 추출
            request.setAttribute("authId", userDetailsImpl.getUsername());
            request.setAttribute("memberId", userDetailsImpl.getId());
            request.setAttribute("userRole", userDetailsImpl.getMember().getRole());
            log.debug("Request Attribute에 사용자 정보 추가 완료: authId={}, memberId={}, userRole={}",
                    userDetailsImpl.getUsername(), userDetailsImpl.getId(), userDetailsImpl.getMember().getRole());
        } else {
            // UserDetailsImpl 타입이 아닌 경우 (일반적이지 않음), 기본 정보만 추가
            log.warn("UserDetails 객체가 UserDetailsImpl 타입이 아닙니다. 클래스: {}. 사용자 이름(authId)만 추가합니다.", userDetails.getClass().getName());
            request.setAttribute("authId", userDetails.getUsername());
        }
    }

    /**
     * 주어진 경로가 SecurityProperties에 정의된 제외 경로 목록과 일치하는지 확인합니다.
     * @param path 검사할 요청 경로
     * @return 제외 경로에 해당하면 true, 아니면 false
     */
    private boolean isExcludedPath(String path) {
        // 제외 경로 목록이 설정되지 않았으면 false 반환
        if (securityProperties.getExcludedPaths() == null) {
            return false;
        }
        // 각 제외 경로 패턴과 현재 경로를 비교
        for (String excludedPath : securityProperties.getExcludedPaths()) {
            if (antPathMatcher.match(excludedPath, path)) {
                // 하나라도 일치하면 true 반환
                return true;
            }
        }
        return false;
    }
}