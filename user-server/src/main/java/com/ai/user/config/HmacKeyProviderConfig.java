package com.ai.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "crypto.hmac")
@Setter
@Getter
public class HmacKeyProviderConfig {
    private Map<String, String> keys;
    private String currentVersion;

    public String getHmacKey(String version) {
        return keys.get(version);
    }
}
