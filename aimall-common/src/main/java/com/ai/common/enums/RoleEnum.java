package com.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN(1, "ADMIN", "管理员"),
    CUSTOMER_SERVICE(2, "CUSTOMER_SERVICE", "客服"),
    USER(3, "USER", "用户");

    private final int code;
    private final String value;
    private final String description;
}
