package com.ai.common.utils;

import java.util.regex.Pattern;

public final class Validators {
    private Validators() {}

    /** 中国大陆手机号：11 位，1 开头，第二位 3-9，无前缀无分隔 */
    private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱：local 首尾字母数字，域名标签首尾字母数字，TLD 2-63 位字母 */
    private static final Pattern EMAIL = Pattern.compile(
        "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@" +
        "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +
        "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*" +
        "\\.[A-Za-z]{2,63}$"
    );

    public static boolean isCnMobile(String phone) {
        return phone != null && CN_MOBILE.matcher(phone).matches();
    }

    public static boolean isEmail(String email) {
        return email != null && email.length() <= 254 && EMAIL.matcher(email).matches();
    }
}