package com.ai.common.exception;

import com.ai.common.enums.StatusCode;

public class UserException extends WarmException {
    public UserException(StatusCode sc) {
        super(sc);
    }

    public UserException(StatusCode sc, String detail) {
        super(sc, detail);
    }

    public UserException(StatusCode sc, String detail, Throwable cause) {
        super(sc, detail);
    }
}
