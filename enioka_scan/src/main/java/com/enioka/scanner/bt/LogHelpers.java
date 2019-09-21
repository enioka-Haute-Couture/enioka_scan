package com.enioka.scanner.bt;

public class LogHelpers {
    public static String byteArrayToHex(byte[] buffer, int length) {
        StringBuilder sb = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x ", buffer[i]));
        }
        return sb.toString();
    }
}