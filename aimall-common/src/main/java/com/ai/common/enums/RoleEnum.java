package com.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN(1, "管理员"),
    CUSTOMER_SERVICE(2, "客服"),
    USER(3, "用户");

    private final int value;
    private final String message;
}
