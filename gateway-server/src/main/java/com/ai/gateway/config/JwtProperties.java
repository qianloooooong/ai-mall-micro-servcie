package com.ai.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.jwt")
public class JwtProperties {
    private String issuer = "http://localhost:18090";
    private String jwkSetUri;
}