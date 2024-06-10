package com.enioka.scanner.sdk.honeywelloss.integrated;

import androidx.annotation.Nullable;

import com.enioka.scanner.data.BarcodeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to translate symbology IDs, common between Honeywell SDKs (bluetooth or integrated).
 */
public final class SymbologyId {
    /**
     * Map associating a honeywell ID (in ASCII hexadecimal value) to its corresponding symbology.
     *
     */
    public static final Map<Integer, String> honeywellIdMap = Collections.unmodifiableMap(new HashMap<Integer, String>(){{
        put(0x2C, "INFOMAIL"); //,
        put(0x2E, "DOTCODE"); //.
        put(0x2D, "MICROQR_ALT"); //-
        put(0x31, "CODE1"); //1
        put(0x3B, "MERGED_COUPON"); //;
        put(0x3C, "CODE32"); //< // Also LABELCODE_V and others
        put(0x3D, "TRIOPTIC"); //=
        put(0x3E, "LABELCODE_IV"); //>
        put(0x3F, "KOREA_POST"); //?
        put(0x41, "AUS_POST"); //A
        put(0x42, "BRITISH_POST"); //B
        put(0x43, "CANADIAN_POST"); //C
        put(0x44, "EAN8"); //D
        put(0x45, "UPCE"); //E // UPC-E, UPC-E1, UPC-E with add-on
        put(0x47, "BC412"); //G
        put(0x48, "HAN_XIN_CODE"); //H
        put(0x49, "GS1_128"); //I
        put(0x4A, "JAPAN_POST"); //J
        put(0x4B, "KIX_POST"); //K
        put(0x4C, "PLANET_CODE"); //L
        put(0x4D, "INTELLIGENT_MAIL"); //M
        put(0x4E, "ID_TAGS"); //N
        put(0x4F, "OCR"); //O
        put(0x50, "POSTNET"); //P
        put(0x51, "CHINA_POST"); //Q
        put(0x52, "MICROPDF"); //R
        put(0x53, "SECURE_CODE"); //S
        put(0x54, "TLC39"); //T
        put(0x55, "ULTRACODE"); //U
        put(0x56, "CODABLOCK_A"); //V
        put(0x57, "POSICODE"); //W
        put(0x58, "GRID_MATRIX"); //X
        put(0x59, "NEC25"); //Y // NEC 2 of 5
        put(0x5A, "MESA"); //Z
        put(0x5B, "SWEEDISH_POST"); //[
        put(0x5D, "BRAZIL_POST"); //]
        put(0x60, "EAN13_ISBN"); //`
        put(0x61, "CODABAR"); //a
        put(0x62, "CODE39"); //b
        put(0x63, "UPCA"); //c // UPC-A, UPC-A with add-on, UPC-A with extended coupon code
        put(0x64, "EAN13"); //d
        put(0x65, "I25"); //e // Interleaved 2 of 5
        put(0x66, "S25"); //f // Straight 2 of 5 IATA or Straight 2 of 5 Industrial
        put(0x67, "MSI"); //g
        put(0x68, "CODE11"); //h
        put(0x69, "CODE93"); //i
        put(0x6A, "CODE128"); //j
        put(0x6B, "UNUSED"); //k
        put(0x6C, "CODE49"); //l
        put(0x6D, "M25"); //m // Matrix 2 of 5
        put(0x6E, "PLESSEY"); //n
        put(0x6F, "CODE16K"); //o
        put(0x70, "CHANNELCODE"); //p
        put(0x71, "CODABLOCK_F"); //q
        put(0x72, "PDF417"); //r
        put(0x73, "QRCODE"); //s
        put(0x74, "TELEPEN"); //t
        put(0x75, "CODEZ"); //u
        put(0x76, "VERICODE"); //v
        put(0x77, "DATAMATRIX"); //w
        put(0x78, "MAXICODE"); //x
        put(0x79, "GS1_DATABAR"); //y // also RSS
        put(0x7A, "AZTEC_CODE"); //z
        put(0x7B, "GS1_DATABAR_LIMITED"); //{
        put(0x7C, "GS1_128"); //| // also RM_MAILMARK instead of GS1_128 for the integrated scanners
        put(0x7D, "GS1_DATABAR_EXPANDED"); //}
    }});

    /**
     * Map associating an AIM ID (in ASCII hexadecimal value) to its corresponding symbology.
     * The modifier is ignored, so the symbology may not be complete (e.g. "UPC/EAN" -> could be EAN-8 or EAN-13).
     * Because of this, it is recommended to prefer using the honeywellIdMap instead.
     */
    public static final Map<Integer, String> AIMIdMap = Collections.unmodifiableMap(new HashMap<Integer, String>() {{
        put(0x41, "CODE39"); //A
        put(0x42, "TELEPEN"); //B
        put(0x43, "CODE128"); //C
        put(0x45, "UPC/EAN"); //E
        put(0x46, "CODABAR"); //F
        put(0x47, "CODE93"); //G
        put(0x48, "CODE11"); //H
        put(0x49, "I25"); //I
        put(0x4C, "PDF417"); //L
        put(0x4D, "MSI"); //M
        put(0x4F, "CODABLOCK"); //O
        put(0x50, "STANDARD"); //P
        put(0x51, "QRCODE"); //Q
        put(0x52, "Standard2of5"); //R
        put(0x53, "D25"); //S
        put(0x54, "CODE49"); //T
        put(0x55, "MAXICODE"); //U
        put(0x58, "TriopticCode39"); //X
        put(0x5A, "NODATA"); //Z
        put(0x64, "DATAMATRIX"); //d
        put(0x65, "GS1"); //e
        put(0x7A, "AZTEC_CODE"); //z
    }});

    /**
     * Converts a string returned by one of the ID maps above into one of the available BarcodeTypes.
     * Defaults to the UNKNOWN type if no corresponding type exists.
     * FIXME: Fill-in missing types to BarcodeType and make maps return a type directly.
     * @param symbologyString The string returned by one of the ID maps.
     * @return The corresponding BarcodeType.
     */
    public static BarcodeType toBarcodeType(@Nullable final String symbologyString) {
        if (symbologyString == null)
            return BarcodeType.UNKNOWN;
        if (symbologyString.equals("CODE128"))
            return BarcodeType.CODE128;
        if (symbologyString.equals("CODE39"))
            return BarcodeType.CODE39;
        if (symbologyString.equals("D25") || symbologyString.equals("S25"))
            return BarcodeType.DIS25;
        if (symbologyString.equals("I25"))
            return BarcodeType.INT25;
        if (symbologyString.equals("EAN13") || symbologyString.equals("UPC/EAN") || symbologyString.equals("EAN13_ISBN"))
            return BarcodeType.EAN13;
        if (symbologyString.equals("QRCODE"))
            return BarcodeType.QRCODE;
        if (symbologyString.equals("AZTEC_CODE"))
            return BarcodeType.AZTEC;
        if (symbologyString.equals("KOREA_POST"))
            return BarcodeType.KOREA_POST;
        if (symbologyString.equals("AUS_POST"))
            return BarcodeType.AUS_POST;
        if (symbologyString.equals("BRITISH_POST"))
            return BarcodeType.BRITISH_POST;
        if (symbologyString.equals("CANADIAN_POST"))
            return BarcodeType.CANADIAN_POST;
        if (symbologyString.equals("EAN8"))
            return BarcodeType.EAN8;
        if (symbologyString.equals("UPCE"))
            return BarcodeType.UPCE;
        if (symbologyString.equals("HAN_XIN_CODE"))
            return BarcodeType.HAN_XIN;
        if (symbologyString.equals("GS1_128"))
            return BarcodeType.GS1_128;
        if (symbologyString.equals("JAPAN_POST"))
            return BarcodeType.JAPAN_POST;
        if (symbologyString.equals("KIX_POST"))
            return BarcodeType.DUTCH_POST;
        if (symbologyString.equals("CHINA_POST"))
            return BarcodeType.CHINA_POST;
        if (symbologyString.equals("GRID_MATRIX"))
            return BarcodeType.GRID_MATRIX;
        if (symbologyString.equals("CODABAR"))
            return BarcodeType.CODABAR;
        if (symbologyString.equals("UPCA"))
            return BarcodeType.UPCA;
        if (symbologyString.equals("MSI"))
            return BarcodeType.MSI;
        if (symbologyString.equals("CODE11"))
            return BarcodeType.CODE11;
        if (symbologyString.equals("CODE93"))
            return BarcodeType.CODE93;
        if (symbologyString.equals("PDF417"))
            return BarcodeType.PDF417;
        if (symbologyString.equals("DATAMATRIX"))
            return BarcodeType.DATAMATRIX;
        if (symbologyString.equals("MAXICODE"))
            return BarcodeType.MAXICODE;
        if (symbologyString.equals("GS1_DATABAR"))
            return BarcodeType.GS1_DATABAR;
        if (symbologyString.equals("GS1_DATABAR_LIMITED"))
            return BarcodeType.GS1_DATABAR_LIMITED;
        if (symbologyString.equals("GS1_DATABAR_EXPANDED"))
            return BarcodeType.GS1_DATABAR_EXPANDED;
        return BarcodeType.UNKNOWN;
    }
}
