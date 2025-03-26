package com.soda.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soda.global.response.ApiResponseForm;
import com.soda.global.response.ErrorCode;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.global.security.config.SecurityProperties;
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
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
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
            sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            sendErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (UsernameNotFoundException e) {
            sendErrorResponse(response, ErrorCode.NOT_FOUND_MEMBER);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            sendErrorResponse(response, ErrorCode.UNEXPECTED_ERROR);
        }
    }

    private void addUserAttributes(HttpServletRequest request, UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
            request.setAttribute("authId", userDetailsImpl.getUsername());
            request.setAttribute("memberId", userDetailsImpl.getId());
            request.setAttribute("userRole", userDetailsImpl.getMember().getRole());
        } else {
            log.warn("UserDetails is not an instance of UserDetailsImpl.");
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
            // 로깅만 하고, 여기서 다시 예외 던지면 또 문제가 생길 수 있으므로 swallow
            System.err.println("sendErrorResponse 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
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