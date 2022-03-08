package com.enioka.scanner.sdk.honeywelloss.helpers;

import android.support.annotation.Nullable;

import com.enioka.scanner.data.BarcodeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SymbologyId {
    /**
     * Map associating a honeywell ID (in ASCII hexadecimal value) to its corresponding symbology.
     */
    public static final Map<Integer, String> honeywellIdMap = Collections.unmodifiableMap(new HashMap<Integer, String>(){{
        put(0x41, "AustralianPost"); //A
        put(0x42, "BritishPost"); //B
        put(0x43, "CanadianPost"); //C
        put(0x44, "EAN-8"); //D
        put(0x45, "UPC-E"); //E
        put(0x48, "ChineseSensibleCode"); //H
        put(0x4A, "JapanesePost"); //J
        put(0x4B, "NetherlandsPost"); //K
        put(0x4C, "PlanetCode"); //L
        put(0x4D, "IntelligentMailBarCode"); //M
        put(0x4E, "Postal-4i"); //N
        put(0x50, "Postnet"); //P
        put(0x51, "ChinaPost"); //Q
        put(0x52, "MicroPDF417"); //R
        put(0x54, "TLC39"); //T
        put(0x56, "CodablockA"); //V
        put(0x59, "NEC2of5"); //Y
        put(0x61, "Codabar"); //a
        put(0x62, "Code39"); //b
        put(0x63, "UPC-A"); //c
        put(0x64, "EAN-13"); //d
        put(0x65, "I2of5"); //e
        put(0x66, "Straight2of5IATA"); //f
        put(0x67, "MSI"); //g
        put(0x68, "Code11"); //h
        put(0x69, "Code93"); //i
        put(0x6A, "Code128"); //j
        put(0x6C, "Code49"); //l
        put(0x6D, "Matrix2of5"); //m
        put(0x71, "CodablockF"); //q
        put(0x72, "PDF417"); //r
        put(0x73, "QRCode"); //s
        put(0x74, "Telepen"); //t
        put(0x77, "DataMatrix"); //w
        put(0x78, "MaxiCode"); //x
        put(0x79, "GS1"); //y
        put(0x7A, "Aztec"); //z
        put(0x7B, "GS1DatabarLimited"); //{
        put(0x7C, "GS1-128"); //|
        put(0x7D, "GS1DatabarExpanded"); //}
        put(0x3C, "Code32Pharmaceutical"); //<
        put(0x2C, "InfoMail"); //,
        put(0x3F, "KoreaPost"); //?
    }});

    /**
     * Map associating an AIM ID (in ASCII hexadecimal value) to its corresponding symbology.
     * The modifier is ignored, so the symbology may not be complete (e.g. "UPC/EAN" -> could be EAN-8 or EAN-13).
     * Because of this, it is recommended to prefer using the honeywellIdMap instead.
     */
    public static final Map<Integer, String> AIMIdMap = Collections.unmodifiableMap(new HashMap<Integer, String>() {{
        put(0x41, "Code39"); //A
        put(0x42, "Telepen"); //B
        put(0x43, "Code128"); //C
        put(0x45, "UPC/EAN"); //E
        put(0x46, "Codabar"); //F
        put(0x47, "Code93"); //G
        put(0x48, "Code11"); //H
        put(0x49, "I2of5"); //I
        put(0x4C, "PDF417"); //L
        put(0x4D, "MSI"); //M
        put(0x4F, "Codablock"); //O
        put(0x50, "Standard"); //P
        put(0x51, "QRCode"); //Q
        put(0x52, "Standard2of5"); //R
        put(0x53, "D2of5"); //S
        put(0x54, "Code49"); //T
        put(0x55, "Maxicode"); //U
        put(0x58, "TriopticCode39"); //X
        put(0x5A, "NODATA"); //Z
        put(0x64, "DataMatrix"); //d
        put(0x65, "GS1"); //e
        put(0x7A, "Aztec"); //z
    }});

    /**
     * Converts a string returned by one of the ID maps above into one of the available BarcodeTypes.
     * Defaults to the UNKNOWN type if no corresponding type exists.
     * FIXME: Fill-in missing types to BarcodeType and make maps return a type directly.
     * @param symbologyString The string returned by one of the ID maps.
     * @return The corresponding BarcodeType.
     */
    public static BarcodeType toBarcodeType(final String symbologyString) {
        if (symbologyString == null)
            throw new IllegalArgumentException("No symbology to convert");
        if (symbologyString.equals("Code128"))
            return BarcodeType.CODE128;
        if (symbologyString.equals("Code39"))
            return BarcodeType.CODE39;
        if (symbologyString.equals("D2of5"))
            return BarcodeType.DIS25;
        if (symbologyString.equals("I2of5"))
            return BarcodeType.INT25;
        if (symbologyString.equals("EAN-13") || symbologyString.equals("UPC/EAN"))
            return BarcodeType.EAN13;
        if (symbologyString.equals("QRCode"))
            return BarcodeType.QRCODE;
        return BarcodeType.UNKNOWN;
    }
}
