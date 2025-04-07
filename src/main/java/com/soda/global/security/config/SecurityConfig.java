package com.soda.global.security.config;

import com.soda.global.security.jwt.JwtAuthenticationFilter;
import com.soda.global.security.jwt.JwtExceptionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Spring Security 설정을 위한 구성 클래스입니다.
 * 웹 보안 활성화, 필터 체인 구성, 비밀번호 인코더 정의 등을 수행합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;

    /**
     * 비밀번호 암호화에 사용할 PasswordEncoder 빈을 등록합니다.
     * 여기서는 BCrypt 알고리즘을 사용합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security의 필터 체인을 구성합니다.
     * HTTP 요청에 대한 보안 규칙(인증/인가, CSRF, 세션 관리 등) 및 필터 적용 순서를 정의합니다.
     *
     * @param http HttpSecurity 객체 (Spring Security 설정을 위한 빌더)
     * @return 구성된 SecurityFilterChain 객체
     * @throws Exception 설정 과정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호 비활성화
                // 이유: REST API는 일반적으로 상태가 없고(stateless) 세션을 사용하지 않으므로,
                //      CSRF 공격에 비교적 안전하며, JWT 토큰 자체로 요청을 인증하므로 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // 세션 관리 정책 설정: STATELESS (상태 없음)
                // 이유: JWT 기반 인증은 서버에 세션을 유지하지 않으므로, 상태 없는 정책을 사용합니다.
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // HTTP Basic 인증 비활성화
                // 이유: JWT 토큰 인증 방식을 사용하므로, 기본 제공되는 HTTP Basic 인증은 필요 없습니다.
                .httpBasic(AbstractHttpConfigurer::disable)

                // 폼 기반 로그인 비활성화
                // 이유: REST API에서는 일반적으로 폼 로그인을 사용하지 않고, 클라이언트가 직접 로그인 요청을 보내고 토큰을 받습니다.
                .formLogin(AbstractHttpConfigurer::disable)

                // 기본 예외 처리 비활성화
                // 이유: 기본 AuthenticationEntryPoint 동작 대신 JwtExceptionFilter에서 인증 예외를 처리합니다.
                .exceptionHandling(AbstractHttpConfigurer::disable)

                // HTTP 요청 인가 규칙 설정
                .authorizeHttpRequests(authorize -> {
                    List<String> excludedPaths = securityProperties.getExcludedPaths();
                    if (excludedPaths != null && !excludedPaths.isEmpty()) {
                        // 제외 경로들에 대해 인증 없이 접근 허용 (permitAll)
                        authorize.requestMatchers(excludedPaths.toArray(new String[0])).permitAll();
                    }
                    // OPTIONS 메소드는 CORS Preflight 요청이므로 모두 허용
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    // 나머지 모든 요청은 인증 필요
                    authorize.anyRequest().authenticated();
                })

                // 사용자 정의 필터를 Spring Security 필터 체인에 추가
                // 순서 중요: JwtExceptionFilter가 JwtAuthenticationFilter 앞에 위치해야,
                //          JwtAuthenticationFilter에서 발생한 예외를 JwtExceptionFilter가 처리할 수 있습니다.
                // UsernamePasswordAuthenticationFilter.class: 이 필터는 기본 폼 로그인 등을 처리하는데,
                //                                         우리의 JWT 필터들은 이보다 먼저 실행되어야 합니다.
                .addFilterBefore(jwtExceptionFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 설정된 HttpSecurity 객체를 기반으로 SecurityFilterChain 빌드 및 반환
        return http.build();
    }
}