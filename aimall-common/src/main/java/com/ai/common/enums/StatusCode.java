package com.ai.common.enums;

import lombok.Getter;

@Getter
public enum StatusCode {
    SUCCESS(200000,"success", "成功"),
    SUCCESS_WITH_DATA(200001,"success", "请求成功并返回数据"),
    SUCCESS_WITHOUT_DATA(200002,"success", "请求成功但不返回数据"),
    SUCCESS_LOGIN(200007,"登录成功", "登录成功"),
    SUCCESS_TOKEN_REFRESH(200008,"success", "刷新令牌成功"),
    SUCCESS_LOGOUT(200009,"登出成功", "登出成功"),
    SUCCESS_GET_USER_LOGIN_INFO(200010,"success", "成功获取用户登录信息"),

    CREATED(201000,"success", "创建成功"),
    SUCCESS_POST_WITH_DATA(201001,"success", "POST请求成功并返回数据"),
    SUCCESS_POST_WITHOUT_DATA(201002,"success", "POST请求成功但不返回数据"),
    SUCCESS_BIND_PHONE(201003,"成功绑定手机", "成功绑定手机"),
    SUCCESS_BIND_EMAIL(201004,"成功绑定邮箱", "成功绑定邮箱"),
    SUCCESS_RESET_PHONE(201005,"成功重置手机", "成功重置手机"),
    SUCCESS_RESET_EMAIL(201006,"成功重置邮箱", "成功重置邮箱"),
    SUCCESS_SEND_PHONE_CODE(201007,"手机验证码已发送", "成功发送手机验证码"),
    SUCCESS_SEND_EMAIL_CODE(201008,"邮箱验证码已发送", "成功发送邮箱验证码"),

    BAD_REQUEST(400000,"参数错误", "参数错误"),

    UNAUTHORIZED(401000,"未授权", "未授权"),
    UNAUTHORIZED_TOKEN_INVALID(401001,"身份认证失败", "token无效"),
    UNAUTHORIZED_TOKEN_EXPIRED(401002,"身份认证失败", "token已过期"),
    UNAUTHORIZED_REFRESH_TOKEN_INVALID(401003,"身份认证失败", "refresh token无效"),
    UNAUTHORIZED_REFRESH_TOKEN_EXPIRED(401004,"身份认证失败", "refresh token已过期"),
    UNAUTHORIZED_LOGIN_INFO_ERROR(401005,"账号或密码错误", "用户名错误"),

    FORBIDDEN(403000,"没有访问权限", "禁止访问"),
    ACCESS_DENIED(403001,"没有访问权限", "用户无访问权限"),

    UNPROCESSABLE_ENTITY(422000,"参数错误", "参数错误"),
    NULL_USER_BIND_PHONE(422001,"手机号码不能为空", "用户绑定手机参数为空"),
    INVALID_USER_BIND_PHONE(422002,"无效的手机号码", "用户绑定手机参数无效"),
    NULL_USER_BIND_EMAIL(422003,"邮箱地址不能为空", "用户绑定邮箱参数为空"),
    INVALID_USER_BIND_EMAIL(422004,"无效的邮箱地址", "用户绑定邮箱参数无效"),
    INVALID_PHONE_CODE(422005,"无效的验证码", "手机验证码无效"),
    INVALID_EMAIL_CODE(422006,"无效的验证码", "邮箱验证码无效"),
    ERROR_PHONE_CODE(422007,"验证码错误", "手机验证码错误"),
    ERROR_EMAIL_CODE(422008,"验证码错误", "邮箱验证码错误"),
    NULL_USERNAME(422009,"参数错误", "用户名参数为空"),
    INVALID_PHONE(422010,"无效的手机号码", "用户手机参数无效"),
    NULL_PHONE(422011,"手机号码不能为空", "用户手机参数为空"),
    INVALID_EMAIL(422012,"无效的邮箱地址", "用户邮箱参数无效"),
    NULL_EMAIL(422013,"邮箱地址不能为空", "用户邮箱参数为空"),

    PHONE_CODE_EXISTED(429001,"操作频繁，请稍候再试", "手机验证码已存在"),
    EMAIL_CODE_EXISTED(429002,"操作频繁，请稍候再试", "邮箱验证码已存在"),

    FAILURE(500000,"服务器内部错误", "失败"),
    UNKNOWN_AES_VERSION(500010,"服务器内部错误", "未知的AES密钥版本"),
    UNKNOWN_HMAC_VERSION(500011,"服务器内部错误", "未知的HMAC密钥版本"),
    ENCRYPT_AES_FAILED(500012,"服务器内部错误", "AES加密失败"),
    DECRYPT_AES_FAILED(500013,"服务器内部错误", "AES解密失败"),
    SIGN_HMAC_FAILED(500014,"服务器内部错误", "HMAC签名失败"),
    NULL_AES_KEY(500015,"服务器内部错误", "AES密钥为空"),
    NULL_HMAC_KEY(500016,"服务器内部错误", "HMAC密钥为空"),
    BIND_PHONE_FAILURE(500017,"绑定手机失败", "绑定手机失败"),
    BIND_PHONE_NOT_FIRST(500018,"绑定手机失败", "非首次绑定手机"),
    PHONE_EXISTED(500019,"手机号码已存在", "手机号码已存在"),
    BIND_EMAIL_FAILURE(500020,"绑定邮箱失败", "绑定邮箱失败"),
    BIND_EMAIL_NOT_FIRST(500021,"绑定邮箱失败", "非首次绑定邮箱"),
    EMAIL_EXISTED(500022,"邮箱地址已存在", "邮箱地址已存在");

    private final int code;
    private final String msg;
    private final String description;

    StatusCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }
}
