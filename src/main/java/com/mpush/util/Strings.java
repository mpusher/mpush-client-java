package com.mpush.util;

/**
 * Created by ohun on 2015/12/23.
 */
public final class Strings {
    public static final String EMPTY = "";

    public static boolean isBlank(CharSequence text) {
        if (text == null || text.length() == 0) return true;
        for (int i = 0, L = text.length(); i < L; i++) {
            if (!Character.isWhitespace(text.charAt(i))) return false;
        }
        return true;
    }

    public static long toLong(String text, long defaultVal) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
        }
        return defaultVal;
    }

    public static int toInt(String text, int defaultVal) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
        }
        return defaultVal;
    }
}
