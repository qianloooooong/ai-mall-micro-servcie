package com.ai.user.utils;

public class MaskedUtil {

    public static String maskedPhone(String phone) {
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    public static String maskedEmail(String email) {
        int at = email.lastIndexOf('@');

        String local  = email.substring(0, at);
        String domain = email.substring(at);
        String prefix = local.length() > 3 ? local.substring(0, 3) : local;
        return prefix + "****" + domain;
    }
}
