package com.ai.common.exception;

import cn.hutool.core.util.StrUtil;
import com.ai.common.enums.StatusCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final StatusCode statusCode;

    public BaseException(StatusCode sc) {
        this(sc, null, null);
    }

    public BaseException(StatusCode statusCode, String detail) {
        this(statusCode, detail, null);
    }

    public BaseException(StatusCode sc, String detail, Throwable cause) {
        super(buildMessage(sc, detail), cause);
        this.statusCode = sc;
    }

    private static String buildMessage(StatusCode statusCode, String detail) {
        return StrUtil.isBlank(detail) ? statusCode.getDescription() : statusCode.getDescription() + ": " + detail;
    }
}
