package com.dhlyf.walletsupport.utils;


public class StringUtil {

    public static String maskEmail(String email) {
        if (!hasText(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "****" + email.substring(atIndex - 1);
    }

    public static String maskMobile(String mobile) {
        if (!hasText(mobile)) {
            return mobile;
        }
        if (mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    public static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
