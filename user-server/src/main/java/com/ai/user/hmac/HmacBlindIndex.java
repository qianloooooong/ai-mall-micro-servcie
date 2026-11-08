package com.ai.user.hmac;// crypto/HmacBlindIndex.java

import com.ai.common.exception.BlindIndexException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static com.ai.common.enums.StatusCode.SIGN_HMAC_FAILED;

@Component
public class HmacBlindIndex {

    public byte[] compute(String plaintext, SecretKey key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)); // 固定 32B
        } catch (Exception e) {
            throw new BlindIndexException(SIGN_HMAC_FAILED);
        }
    }
}