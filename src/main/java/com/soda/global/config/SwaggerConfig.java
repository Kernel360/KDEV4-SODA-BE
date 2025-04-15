package com.soda.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.s0da.co.kr", description = "Production Server"),
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        }
)
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("accessToken", new SecurityScheme()
                                .name("Authorization") // 헤더 이름
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("accessToken")) // 요청에 SecurityScheme 적용
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("SODA")
                .description("SODA API 명세서")
                .version("1.0.0");
    }
}
