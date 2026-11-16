package com.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    USERNAME_PASSWORD("up", "用户名、密码登录"),
    EMAIL_PASSWORD("ep", "邮箱、密码登录"),
    MOBILE_PASSWORD("mp", "手机、密码登录"),
    EMAIL_CODE("ec", "邮箱、验证码登录"),
    MOBILE_CODE("mc", "手机、验证码登录");

    private final String type;
    private final String description;

    public static boolean isCodeLogin(String type) {
        return EMAIL_CODE.getType().equals(type) || MOBILE_CODE.getType().equals(type) ;
    }
}
