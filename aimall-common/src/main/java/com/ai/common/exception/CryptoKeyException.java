package com.ai.common.exception;

import com.ai.common.enums.StatusCode;

public class CryptoKeyException extends BaseException {
    public CryptoKeyException(StatusCode sc) {
        super(sc);
    }

    public CryptoKeyException(StatusCode sc, String detail) {
        super(sc, detail);
    }

    public CryptoKeyException(StatusCode sc, String detail, Throwable cause) {
        super(sc, detail);
    }
}
