package com.ai.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aimall.frontend")
@Data
public class FrontendConfig {
    private String url;
}
