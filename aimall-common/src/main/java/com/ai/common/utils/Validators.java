package com.ai.common.utils;

import java.util.regex.Pattern;

public final class Validators {
    private Validators() {}

    /** 中国大陆手机号：11 位，1 开头，第二位 3-9，无前缀无分隔 */
    private static final Pattern CN_MOBILE = Pattern.compile("\\A1[3-9]\\d{9}\\z");

    /** 邮箱：local 首尾字母数字，域名标签首尾字母数字，TLD 2-63 位字母 */
    private static final Pattern EMAIL = Pattern.compile(
            "\\A(?=.{1,254}\\z)(?![^@]*\\.\\.)" +
            "[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@" +
            "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+" +
            "[A-Za-z]{2,63}\\z"
    );

    /** 用户名：长度最短8位，最长20位，只允许包含字母、数字、下划线、点 **/
    private static final Pattern USERNAME = Pattern.compile("\\A[A-Za-z0-9._]{8,20}\\z");

    /** 密码：长度 8~72，只允许数字、字母和所有英文符号（不含空格） */
    private static final Pattern PASSWORD = Pattern.compile("\\A[!-~]{8,72}\\z");

    public static boolean isCnMobile(String phone) {
        return phone != null && CN_MOBILE.matcher(phone).matches();
    }

    public static boolean isEmail(String email) {
        return email != null && email.length() <= 254 && EMAIL.matcher(email).matches();
    }

    public static boolean isUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isPassword(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }
}