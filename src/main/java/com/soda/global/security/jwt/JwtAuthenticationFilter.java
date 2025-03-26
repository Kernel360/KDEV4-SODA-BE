package com.soda.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.global.response.ApiResponseForm;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.ErrorCode;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.global.security.config.SecurityProperties;
import com.soda.member.error.AuthErrorCode;
import com.soda.member.error.MemberErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (isExcludedPath(request.getServletPath())) {
                filterChain.doFilter(request, response);
                return;
            }
            // 토큰 검증 시도
            if (!jwtTokenProvider.validateToken(token)) {
                sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
                return;
            }

            // 토큰이 유효한 경우에만 인증 처리
            Claims claims = jwtTokenProvider.getClaims(token);
            String authId = claims.getSubject();

            UserDetails userDetails = userDetailsService.loadUserByUsername(authId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // request에 사용자 정보 추가
            addUserAttributes(request, userDetails);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("JWT 인증 필터 중 토큰 만료 에러 발생: {}", e.getMessage(), e);
            sendErrorResponse(response, AuthErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("JWT 인증 필터 중 유효하지 않은 토큰 에러 발생: {}", e.getMessage(), e);
            sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
        } catch (UsernameNotFoundException e) {
            log.error("JWT 인증 필터 중 멤버를 찾을 수 없음: {}", e.getMessage(), e);
            sendErrorResponse(response, MemberErrorCode.NOT_FOUND_MEMBER);
        } catch (Exception e) {
            log.error("JWT 인증 필터 중 인증 실패 에러 발생: {}", e.getMessage(), e);
            sendErrorResponse(response, CommonErrorCode.UNEXPECTED_ERROR);
        }
    }

    private void addUserAttributes(HttpServletRequest request, UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
            request.setAttribute("authId", userDetailsImpl.getUsername());
            request.setAttribute("memberId", userDetailsImpl.getId());
            request.setAttribute("userRole", userDetailsImpl.getMember().getRole());
        } else {
            log.warn("UserDetailsImpl 타입의 인스턴스가 아닙니다.");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) {
        try {
            if (response.isCommitted()) {
                return; // 이미 응답이 전송되었으면 처리하지 않음
            }

            response.setStatus(errorCode.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponseForm<Void> errorResponse = ApiResponseForm.error(errorCode.getCode(), errorCode.getMessage());
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);

            OutputStream out = response.getOutputStream();
            out.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e) {
            log.error("sendErrorResponse 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    private boolean isExcludedPath(String path) {
        for (String excludedPath : securityProperties.getExcludedPaths()) {
            if (antPathMatcher.match(excludedPath, path)) {
                return true;
            }
        }
        return false;
    }
}