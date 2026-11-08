package com.ai.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

/**
 * 认证服务 JWT 配置：密钥库、令牌有效期与签发者。
 */
@Data
@ConfigurationProperties(prefix = "ai.jwt")
public class JwtProperties {
    private Resource location;
    private String password;
    private String alias;
    private Duration tokenTTL;
    private Duration refreshTokenTTL;
    private String issuer = "http://localhost:18090";
}
