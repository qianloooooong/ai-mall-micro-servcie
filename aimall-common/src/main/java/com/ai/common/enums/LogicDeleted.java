package com.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogicDeleted {
    NOT_DELETED(0, "未删除"),
    DELETED(Long.MAX_VALUE, "已删除");

    private final long value;
    private final String message;

    public static boolean isDeleted(long value) {
        return value != NOT_DELETED.getValue();
    }
}
