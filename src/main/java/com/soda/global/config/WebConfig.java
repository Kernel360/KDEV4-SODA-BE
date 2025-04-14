package com.soda.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // CORS를 적용할 경로 패턴
                .allowedOrigins("http://localhost:5173", "https://soda-sooty.vercel.app/","http://api.s0da.co.kr/") // 허용할 출처
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 메소드
                .allowedHeaders("*") // 허용할 헤더
                .allowCredentials(true) // 쿠키/인증 정보 허용 여부
                .exposedHeaders("Authorization") // 노출할 헤더 이름 명시
                .maxAge(3600);
    }
}
