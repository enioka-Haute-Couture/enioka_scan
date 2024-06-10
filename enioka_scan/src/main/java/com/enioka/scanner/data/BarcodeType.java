package com.enioka.scanner.data;

/**
 * The different symbologies allowed by this library.
 */
public enum BarcodeType {
    /**
     * Code 128 (standard, not GS1-128 or other variants)
     */
    CODE11("CODE11"), // CODE 11
    CODE39("CODE39"), // CODE 39
    CODE39_FULL_ASCII("CODE39-FULL_ASCII"), // CODE 39 FULL ASCII
    CODE93("CODE93"), // CODE 93
    CODE128("CODE128"), // CODE 128
    DIS25("DIS25"), // DISCRETE 2 OF 5
    INT25("INT25"), // INTERLEAVED 2 OF 5
    EAN13("EAN13"), // EAN 13
    QRCODE("QRCODE"), // QR CODE (normal)
    AZTEC("AZTEC"), // AZTEC 2D
    ISBN10("ISBN10"), // ISBN10
    AZTEC_RUNE("AZTEC_RUNE"), // AZTEC RUNE
    KOREA_POST("KOREA_POST"), // KOREA_POST
    AUS_POST("AUS_POST"), // AUS_POST
    BRITISH_POST("BRITISH_POST"), // BRITISH_POST
    CANADIAN_POST("CANADIAN_POST"), // CANADIAN_POST
    DUTCH_POST("DUTCH_POST"), // DUTCH_POST
    FINISH_POST("FINISH_POST"), // DUTCH_POST
    EAN8("EAN8"), // EAN8
    UPCE("UPCE"), // UPCE
    HAN_XIN("HAN_XIN"), // HAN_XIN
    JAPAN_POST("JAPAN_POST"), // JAPAN_POST
    CHINA_POST("CHINA_POST"), // CHINA_POST
    GRID_MATRIX("GRID_MATRIX"), // GRID_MATRIX
    CODABAR("CODABAR"), // CODABAR
    UPCA("UPCA"), // UPCA
    MSI("MSI"), // MSI
    PDF417("PDF417"), // PDF417
    DATAMATRIX("DATAMATRIX"), // DATAMATRIX
    MAXICODE("MAXICODE"), // MAXICODE
    GS1_DATABAR("GS1_DATABAR"), // GS1_DATABAR
    GS1_DATABAR_LIMITED("GS1_DATABAR_LIMITED"), // GS1_DATABAR_LIMITED
    GS1_DATABAR_EXPANDED("GS1_DATABAR_EXPANDED"), // GS1_DATABAR_EXPANDED
    GS1_128("GS1_128"), // GS1_128

    /**
     * On a result, means the scanner has not returned the information. On a scanner configuration, means the scanner should allow all codes.
     */
    UNKNOWN("UNKNOWN CODE");

    public final String code;

    BarcodeType(String code) {
        this.code = code;
    }
}
