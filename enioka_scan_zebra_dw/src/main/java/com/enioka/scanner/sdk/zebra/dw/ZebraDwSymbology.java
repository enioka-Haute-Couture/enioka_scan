package com.enioka.scanner.sdk.zebra.dw;

import com.enioka.scanner.data.BarcodeType;

/**
 * Index for all DW symbologies. Key name is the name returned by the scanner
 */
enum ZebraDwSymbology {
    // Items supported by the lib
    CODE39("LABEL-TYPE-CODE39", "decoder_code39", BarcodeType.CODE39),
    CODE128("LABEL-TYPE-CODE128", "decoder_code128", BarcodeType.CODE128),
    DIS25("LABEL-TYPE-D2OF5", "decoder_d2of5", BarcodeType.DIS25),
    INT25("LABEL-TYPE-I2OF5", "decoder_i2of5", BarcodeType.INT25),
    EAN13("LABEL-TYPE-EAN13", "decoder_ean13", BarcodeType.EAN13),
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
