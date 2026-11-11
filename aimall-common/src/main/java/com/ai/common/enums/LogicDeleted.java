package com.ai.common.enums;

import lombok.Getter;

@Getter
public enum LogicDeleted {
    NOT_DELETED(0, "未删除"),
    DELETED(Long.MAX_VALUE, "已删除");

    private final long code;
    private final String message;

    LogicDeleted(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public static boolean isDeleted(long code) {
        return code != NOT_DELETED.getCode();
    }
}
