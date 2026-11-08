package com.ai.user.keyprovider;

import cn.hutool.core.util.StrUtil;
import com.ai.common.exception.CryptoKeyException;
import com.ai.user.config.AseKeyProviderConfig;
import com.ai.user.config.HmacKeyProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static com.ai.common.enums.StatusCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NacosKeyProvider implements KeyProvider {
    private final AseKeyProviderConfig aseKeyProviderConfig;
    private final HmacKeyProviderConfig hmacKeyProviderConfig;

    @Override
    public SecretKey getAesKey(String version) {
        if (StrUtil.isBlank(version)) {
            log.error(NULL_AES_KEY.getDescription());
            throw new CryptoKeyException(NULL_AES_KEY);
        }
        String aseKey = aseKeyProviderConfig.getAseKey(version);
        if (aseKey == null) {
//            log.error("{}: {}", UNKNOWN_AES_VERSION.getDescription(), version);
            throw new CryptoKeyException(UNKNOWN_AES_VERSION, version);
        }
        return new SecretKeySpec(aseKey.getBytes(StandardCharsets.UTF_8), "AES");
    }

    @Override
    public SecretKey getHmacKey(String version) {
        if (StrUtil.isBlank(version)) {
            log.error(NULL_HMAC_KEY.getDescription());
            throw new CryptoKeyException(NULL_HMAC_KEY);
        }
        String hmacKey = hmacKeyProviderConfig.getHmacKey(version);
        if (hmacKey == null) {
//            log.error("{}: {}", UNKNOWN_HMAC_VERSION.getDescription(), version);
            throw new CryptoKeyException(UNKNOWN_HMAC_VERSION, version);
        }
        return new SecretKeySpec(hmacKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    public String aesCurrentVersion() {
        return aseKeyProviderConfig.getCurrentVersion();
    }

    @Override
    public String hmacCurrentVersion() {
        return hmacKeyProviderConfig.getCurrentVersion();
    }
}
