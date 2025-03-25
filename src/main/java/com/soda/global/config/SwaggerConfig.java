package com.soda.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@OpenAPIDefinition(
//        servers = {
//                @Server(url = "ec2-54-180-108-126.ap-northeast-2.compute.amazonaws.com", description = "Production Server")
//        }
//)
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
