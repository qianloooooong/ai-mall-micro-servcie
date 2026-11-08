package com.ai.common.enums;

import lombok.Getter;

@Getter
public enum StatusCode {
    SUCCESS(200000,"success", "成功"),
    SUCCESS_WITH_DATA(200001,"success", "请求成功并返回数据"),
    SUCCESS_WITHOUT_DATA(200002,"success", "请求成功但不返回数据"),
    SUCCESS_LOGIN(200007,"success", "登录成功"),
    SUCCESS_TOKEN_REFRESH(200008,"success", "刷新令牌成功"),
    SUCCESS_LOGOUT(200009,"success", "登出成功"),

    CREATED(201000,"success", "创建成功"),
    SUCCESS_POST_WITH_DATA(201001,"success", "POST请求成功并返回数据"),
    SUCCESS_POST_WITHOUT_DATA(201002,"success", "POST请求成功但不返回数据"),

    BAD_REQUEST(400000,"参数错误", "参数错误"),
    USERNAME_ARGUMENT_NULL(400001,"参数错误", "用户名参数为空"),

    UNAUTHORIZED(401000,"未授权", "未授权"),
    UNAUTHORIZED_TOKEN_INVALID(401001,"身份认证失败", "token无效"),
    UNAUTHORIZED_TOKEN_EXPIRED(401002,"身份认证失败", "token已过期"),
    UNAUTHORIZED_REFRESH_TOKEN_INVALID(401003,"身份认证失败", "refresh token无效"),
    UNAUTHORIZED_REFRESH_TOKEN_EXPIRED(401004,"身份认证失败", "refresh token已过期"),
    UNAUTHORIZED_LOGIN_INFO_ERROR(401005,"账号或密码错误", "用户名错误"),

    FORBIDDEN(403000,"没有访问权限", "禁止访问"),
    ACCESS_DENIED(403001,"没有访问权限", "用户无访问权限"),

    FAILURE(500000,"服务器内部错误", "失败"),
    UNKNOWN_AES_VERSION(500010,"服务器内部错误", "未知的AES密钥版本"),
    UNKNOWN_HMAC_VERSION(500011,"服务器内部错误", "未知的HMAC密钥版本"),
    ENCRYPT_AES_FAILED(500012,"服务器内部错误", "AES加密失败"),
    DECRYPT_AES_FAILED(500013,"服务器内部错误", "AES解密失败"),
    SIGN_HMAC_FAILED(500014,"服务器内部错误", "HMAC签名失败"),
    NULL_AES_KEY(500015,"服务器内部错误", "AES密钥为空"),
    NULL_HMAC_KEY(500016,"服务器内部错误", "HMAC密钥为空");

    private final int code;
    private final String msg;
    private final String description;

    StatusCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }
}
