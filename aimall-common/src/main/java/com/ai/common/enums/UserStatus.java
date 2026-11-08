package com.ai.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final int code;
    private final String message;

    UserStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
