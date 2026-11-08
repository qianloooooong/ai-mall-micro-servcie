package com.ai.common.exception;

import com.ai.common.enums.StatusCode;

public class BlindIndexException extends BaseException {
    public BlindIndexException(StatusCode sc) {
        super(sc);
    }

    public BlindIndexException(StatusCode sc, String detail) {
        super(sc, detail);
    }

    public BlindIndexException(StatusCode sc, String detail, Throwable cause) {
        super(sc, detail, cause);
    }
}