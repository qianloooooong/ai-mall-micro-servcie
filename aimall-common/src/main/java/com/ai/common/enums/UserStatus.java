package com.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final int value;
    private final String message;
}
