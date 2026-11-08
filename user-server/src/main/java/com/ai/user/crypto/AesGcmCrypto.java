package com.ai.user.crypto;// crypto/AesGcmCrypto.java

import cn.hutool.core.util.ArrayUtil;
import com.ai.common.exception.CryptoKeyException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static com.ai.common.enums.StatusCode.DECRYPT_AES_FAILED;
import static com.ai.common.enums.StatusCode.ENCRYPT_AES_FAILED;

@Component
public class AesGcmCrypto {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;   // 字节
    private static final int TAG_LENGTH = 128; // 位
    private final SecureRandom random = new SecureRandom();

    /** 返回 IV(12B) + CipherText(含Tag)，直接写入 VARBINARY 列 */
    public byte[] encrypt(String plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[iv.length + cipherText.length];
            ArrayUtil.copy(iv, 0, out, 0, iv.length);
            ArrayUtil.copy(cipherText, 0, out, iv.length, cipherText.length);

            return out;
        } catch (Exception e) {
            throw new CryptoKeyException(ENCRYPT_AES_FAILED);
        }
    }

    /** 从 IV(12B) + CipherText 还原明文 */
    public String decrypt(byte[] encrypted, SecretKey key) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[encrypted.length - IV_LENGTH];
            ArrayUtil.copy(encrypted, 0, iv, 0, IV_LENGTH);
            ArrayUtil.copy(encrypted, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoKeyException(DECRYPT_AES_FAILED, "encryptedLength = " + (encrypted == null ? -1 : encrypted.length), e);
        }
    }
}