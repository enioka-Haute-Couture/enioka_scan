package com.enioka.scanner.data;

/**
 * The different symbologies allowed by this library.
 */
public enum BarcodeType {
    CODE128("CODE128"), // CODE 128
    CODE39("CODE39"), // CODE 39
    DIS25("DIS25"), // DISCRETE 2 OF 5
    INT25("INT25"), // INTERLEAVED 2 OF 5
    EAN13("EAN13"); // EAN 13

    public final String code;

    BarcodeType(String code) {
        this.code = code;
    }

    private String code() {
        return code;
    }
}
