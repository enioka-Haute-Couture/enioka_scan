package com.enioka.scanner.data;

/**
 * The different symbologies allowed by this library.
 */
public enum BarcodeType {
    /**
     * Code 128 (standard, not GS1-128 or other variants)
     */
    CODE128("CODE128"),
    CODE39("CODE39"), // CODE 39
    DIS25("DIS25"), // DISCRETE 2 OF 5
    INT25("INT25"), // INTERLEAVED 2 OF 5
    EAN13("EAN13"), // EAN 13
    /**
     * On a result, means the scanner has not returned the information. On a scanner configuration, means the scanner should allow all codes.
     */
    UNKNOWN("UNKNOWN CODE");

    public final String code;

    BarcodeType(String code) {
        this.code = code;
    }
}
