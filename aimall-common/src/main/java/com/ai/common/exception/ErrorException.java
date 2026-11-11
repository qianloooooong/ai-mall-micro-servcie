package com.ai.common.exception;

import com.ai.common.enums.StatusCode;

public class ErrorException extends BaseException{
    public ErrorException(StatusCode sc) {
        super(sc);
    }

    public ErrorException(StatusCode statusCode, String detail) {
        super(statusCode, detail);
    }

    public ErrorException(StatusCode sc, String detail, Throwable cause) {
        super(sc, detail, cause);
    }
}
