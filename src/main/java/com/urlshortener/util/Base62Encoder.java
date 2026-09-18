package com.urlshortener.util;

public class Base62Encoder {

    private static final String CHARACTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long value) {

        StringBuilder sb = new StringBuilder();

        while (value > 0) {
            sb.append(CHARACTERS.charAt((int)(value % 62)));
            value /= 62;
        }

        return sb.reverse().toString();
    }
}