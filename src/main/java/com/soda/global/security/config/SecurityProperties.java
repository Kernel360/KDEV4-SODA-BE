package com.soda.global.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.security")
@Data
public class SecurityProperties {
    private List<String> excludedPaths;
}
