package com.ai.common.exception;

import com.ai.common.enums.StatusCode;

public class IdWorkerException extends ErrorException {
    public IdWorkerException(StatusCode sc) {
        super(sc);
    }

    public IdWorkerException(StatusCode statusCode, String detail) {
        super(statusCode, detail);
    }

    public IdWorkerException(StatusCode sc, String detail, Throwable cause) {
        super(sc, detail, cause);
    }
}
