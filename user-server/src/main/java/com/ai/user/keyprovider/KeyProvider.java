package com.ai.user.keyprovider;

import javax.crypto.SecretKey;

public interface KeyProvider {
    SecretKey getAesKey(String version);

    SecretKey getHmacKey(String version);

    String aesCurrentVersion();

    String hmacCurrentVersion();
}