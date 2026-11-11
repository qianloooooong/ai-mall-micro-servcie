package com.ai.common.exception;

import com.ai.common.enums.StatusCode;

public class WarmException extends BaseException {

    public WarmException(StatusCode sc) {
        super(sc);
    }

    public WarmException(StatusCode statusCode, String detail) {
        super(statusCode, detail);
    }

    public WarmException(StatusCode sc, String detail, Throwable cause) {
        super(sc, detail, cause);
    }
}
