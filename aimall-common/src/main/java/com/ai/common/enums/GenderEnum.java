package com.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenderEnum {
    NON_SETTING(0, "未设置"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int value;
    private final String desc;
}
