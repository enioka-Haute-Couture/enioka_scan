package com.enioka.scanner.sdk.zebra.dw;

import com.enioka.scanner.data.BarcodeType;

/**
 * Index for all DW symbologies. Key name is the name returned by the scanner
 */
enum ZebraDwSymbology {
    // Items supported by the lib
    CODE128("LABEL-TYPE-CODE128", "decoder_code128", BarcodeType.CODE128),
    CODE39("LABEL-TYPE-CODE39", "decoder_code39", BarcodeType.CODE39),
    DIS25("LABEL-TYPE-D2OF5", "decoder_d2of5", BarcodeType.DIS25),
    INT25("LABEL-TYPE-I2OF5", "decoder_i2of5", BarcodeType.INT25),
    EAN13("LABEL-TYPE-EAN13", "decoder_ean13", BarcodeType.EAN13),
    QRCODE("LABEL-TYPE-QRCODE", "decoder_qrcode", BarcodeType.QRCODE),
    AZTEC("LABEL-TYPE-AZTEC", "decoder_aztec", BarcodeType.AZTEC),
    CODABAR("LABEL-TYPE-CODABAR", "decoder_codabar", BarcodeType.CODABAR),
    CODE93("LABEL-TYPE-CODE93", "decoder_code93", BarcodeType.CODE93),
    UPCA("LABEL-TYPE-UPCA", "decoder_upca", BarcodeType.UPCA),
    ENA8("LABEL-TYPE-EAN8", "decoder_ean8", BarcodeType.EAN8),
    MSI("LABEL-TYPE-MSI", "decoder_msi", BarcodeType.MSI),
    EAN128("LABEL-TYPE-EAN128", "decoder_ean128", BarcodeType.GS1_128),
    DATABAR_COUPON("LABEL-TYPE-DATABAR-COUPON", "decoder_databar_coupon", BarcodeType.GS1_DATABAR),
    ISBT128("LABEL-TYPE-ISBT128", "decoder_isbt128", BarcodeType.CODE128),
    PDF417("LABEL-TYPE-PDF417", "decoder_pdf417", BarcodeType.PDF417),
    CODE11("LABEL-TYPE-CODE11", "decoder_code11", BarcodeType.CODE11),
    MAXICODE("LABEL-TYPE-MAXICODE", "decoder_maxicode", BarcodeType.MAXICODE),
    DATAMATRIX("LABEL-TYPE-DATAMATRIX", "decoder_datamatrix", BarcodeType.DATAMATRIX),
    GS1_DATABAR("LABEL-TYPE-GS1-DATABAR", "decoder_gs1_databar", BarcodeType.GS1_DATABAR),
    GS1_DATABAR_LIM("LABEL-TYPE-GS1-DATABAR-LIM", "decoder_gs1_databar_lim", BarcodeType.GS1_DATABAR_LIMITED),
    GS1_DATABAR_EXP("LABEL-TYPE-GS1-DATABAR-EXP", "decoder_gs1_databar_exp", BarcodeType.GS1_DATABAR_EXPANDED),
    UKPOSTAL("LABEL-TYPE-UKPOSTAL", "decoder_ukpostal", BarcodeType.BRITISH_POST),
    JAPPOSTAL("LABEL-TYPE-JAPPOSTAL", "decoder_jappostal", BarcodeType.JAPAN_POST),
    AUSPOSTAL("LABEL-TYPE-AUSPOSTAL", "decoder_auspostal", BarcodeType.AUS_POST),
    DUTCHPOSTAL("LABEL-TYPE-DUTCHPOSTAL", "decoder_dutchpostal", BarcodeType.DUTCH_POST),
    FINNISHPOSTAL_4S("LABEL-TYPE-FINNISHPOSTAL-4S", "decoder_finnishpostal_4s", BarcodeType.FINISH_POST),
    CANPOSTAL("LABEL-TYPE-CANPOSTAL", "decoder_canpostal", BarcodeType.CANADIAN_POST),
    // Need to be checked
    KOREAN_3OF5("LABEL-TYPE-KOREAN-3OF5", "decoder_korean_3of5", BarcodeType.KOREA_POST),
    HANXIN("LABEL-TYPE-HANXIN", "decoder_hanxin", BarcodeType.HAN_XIN),
    MAILMARK("LABEL-TYPE-MAILMARK", "decoder_mailmark", BarcodeType.BRITISH_POST),
    GRIDMATRIX("LABEL-TYPE-GRIDMATRIX", "decoder_gridmatrix", BarcodeType.GRID_MATRIX),
    UPCE1("LABEL-TYPE-UPCE1", "decoder_upce1", BarcodeType.UPCE),

    UNKNOWN("none", "none", BarcodeType.UNKNOWN);

    // Items not supported by the lib. Present because we need to be able to disable them.
    /*
                    "LABEL-TYPE-CODABAR"
                    "LABEL-TYPE-CODE128"
                    "LABEL-TYPE-D2OF5"
                    "LABEL-TYPE-IATA2OF5"
                    "LABEL-TYPE-I2OF5"
                    "LABEL-TYPE-CODE93"
                    "LABEL-TYPE-UPCA"
                    "LABEL-TYPE-UPCE0"
                    "LABEL-TYPE-UPCE1"
                    "LABEL-TYPE-EAN8"
                    "LABEL-TYPE-EAN13"
                    "LABEL-TYPE-MSI"
                    "LABEL-TYPE-EAN128"
                    "LABEL-TYPE-TRIOPTIC39"
                    "LABEL-TYPE-BOOKLAND"
                    "LABEL-TYPE-COUPON"
                    "LABEL-TYPE-DATABAR-COUPON"
                    "LABEL-TYPE-ISBT128"
                    "LABEL-TYPE-CODE32"
                    "LABEL-TYPE-PDF417"
                    "LABEL-TYPE-MICROPDF"
                    "LABEL-TYPE-TLC39"
                    "LABEL-TYPE-CODE11"
                    "LABEL-TYPE-MAXICODE"
                    "LABEL-TYPE-DATAMATRIX"
                    "LABEL-TYPE-QRCODE"
                    "LABEL-TYPE-GS1-DATABAR"
                    "LABEL-TYPE-GS1-DATABAR-LIM"
                    "LABEL-TYPE-GS1-DATABAR-EXP"
                    "LABEL-TYPE-USPOSTNET"
                    "LABEL-TYPE-USPLANET"
                    "LABEL-TYPE-UKPOSTAL"
                    "LABEL-TYPE-JAPPOSTAL"
                    "LABEL-TYPE-AUSPOSTAL"
                    "LABEL-TYPE-DUTCHPOSTAL"
                    "LABEL-TYPE-FINNISHPOSTAL-4S"
                    "LABEL-TYPE-CANPOSTAL"
                    "LABEL-TYPE-CHINESE-2OF5"
                    "LABEL-TYPE-AZTEC"
                    "LABEL-TYPE-MICROQR"
                    "LABEL-TYPE-US4STATE"
                    "LABEL-TYPE-US4STATE-FICS"
                    "LABEL-TYPE-COMPOSITE-AB"
                    "LABEL-TYPE-COMPOSITE-C"
                    "LABEL-TYPE-WEBCODE"
                    "LABEL-TYPE-SIGNATURE"
                    "LABEL-TYPE-KOREAN-3OF5"
                    "LABEL-TYPE-MATRIX-2OF5"
                    "LABEL-TYPE-OCR"
                    "LABEL-TYPE-HANXIN"
                    "LABEL-TYPE-MAILMARK"
                    "MULTICODE-DATA-FORMAT"
                    "LABEL-TYPE-GS1-DATAMATRIX"
                    "LABEL-TYPE-GS1-QRCODE"
                    "LABEL-TYPE-DOTCODE"
                    "LABEL-TYPE-GRIDMATRIX"
                    "LABEL-TYPE-UNDEFINED" */

    public final BarcodeType type;
    public final String intentExtraReadBarcodeTypeName;
    public final String intentExtraDecoderConfigName;


    ZebraDwSymbology(String intentExtraReadBarcodeTypeName, String intentExtraDecoderConfigName, BarcodeType type) {
        this.type = type;
        this.intentExtraReadBarcodeTypeName = intentExtraReadBarcodeTypeName;
        this.intentExtraDecoderConfigName = intentExtraDecoderConfigName;
    }

    public static ZebraDwSymbology getSymbology(BarcodeType type) {
        for (ZebraDwSymbology s : ZebraDwSymbology.values()) {
            if (type.equals(s.type)) {
                return s;
            }
        }
        return null;
    }

    public static ZebraDwSymbology getSymbology(String intentExtraValue) {
        for (ZebraDwSymbology s : ZebraDwSymbology.values()) {
            if (intentExtraValue.equals(s.intentExtraReadBarcodeTypeName)) {
                return s;
            }
        }
        return UNKNOWN;
    }
}
