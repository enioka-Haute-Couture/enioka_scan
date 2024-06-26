package com.enioka.scanner.sdk.honeywelloss.spp;

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
     */
    private static final Map<String, BarcodeType> honeywell2Lib;

    /**
     * Map associating a honeywell ID (in ASCII hexadecimal value) to its corresponding symbology.
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
        put(0x45, "UPCE"); //E
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
        put(0x59, "NEC25"); //Y
        put(0x5A, "MESA"); //Z
        put(0x5B, "SWEEDISH_POST"); //[
        put(0x5D, "BRAZIL_POST"); //]
        put(0x60, "EAN13_ISBN"); //`
        put(0x61, "CODABAR"); //a
        put(0x62, "CODE39"); //b
        put(0x63, "UPCA"); //c
        put(0x64, "EAN13"); //d
        put(0x65, "I25"); //e
        put(0x66, "S25"); //f
        put(0x67, "MSI"); //g
        put(0x68, "CODE11"); //h
        put(0x69, "CODE93"); //i
        put(0x6A, "CODE128"); //j
        put(0x6B, "UNUSED"); //k
        put(0x6C, "CODE49"); //l
        put(0x6D, "M25"); //m
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

    static {
        honeywell2Lib = new HashMap<>();
        honeywell2Lib.put("CODE128", BarcodeType.CODE128);
        honeywell2Lib.put("CODE39", BarcodeType.CODE39);
        honeywell2Lib.put("D25", BarcodeType.DIS25);
        honeywell2Lib.put("S25", BarcodeType.DIS25);
        honeywell2Lib.put("I25", BarcodeType.INT25);
        honeywell2Lib.put("EAN13", BarcodeType.EAN13);
        honeywell2Lib.put("UPC/EAN", BarcodeType.EAN13);
        honeywell2Lib.put("EAN13_ISBN", BarcodeType.EAN13);
        honeywell2Lib.put("QRCODE", BarcodeType.QRCODE);
        honeywell2Lib.put("AZTEC_CODE", BarcodeType.AZTEC);
        honeywell2Lib.put("KOREA_POST", BarcodeType.KOREA_POST);
        honeywell2Lib.put("AUS_POST", BarcodeType.AUS_POST);
        honeywell2Lib.put("BRITISH_POST", BarcodeType.BRITISH_POST);
        honeywell2Lib.put("CANADIAN_POST", BarcodeType.CANADIAN_POST);
        honeywell2Lib.put("EAN8", BarcodeType.EAN8);
        honeywell2Lib.put("UPCE", BarcodeType.UPCE);
        honeywell2Lib.put("HAN_XIN_CODE", BarcodeType.HAN_XIN);
        honeywell2Lib.put("GS1_128", BarcodeType.GS1_128);
        honeywell2Lib.put("JAPAN_POST", BarcodeType.JAPAN_POST);
        honeywell2Lib.put("KIX_POST", BarcodeType.DUTCH_POST);
        honeywell2Lib.put("CHINA_POST", BarcodeType.CHINA_POST);
        honeywell2Lib.put("GRID_MATRIX", BarcodeType.GRID_MATRIX);
        honeywell2Lib.put("CODABAR", BarcodeType.CODABAR);
        honeywell2Lib.put("UPCA", BarcodeType.UPCA);
        honeywell2Lib.put("MSI", BarcodeType.MSI);
        honeywell2Lib.put("CODE11", BarcodeType.CODE11);
        honeywell2Lib.put("CODE93", BarcodeType.CODE93);
        honeywell2Lib.put("PDF417", BarcodeType.PDF417);
        honeywell2Lib.put("DATAMATRIX", BarcodeType.DATAMATRIX);
        honeywell2Lib.put("MAXICODE", BarcodeType.MAXICODE);
        honeywell2Lib.put("GS1_DATABAR", BarcodeType.GS1_DATABAR);
        honeywell2Lib.put("GS1_DATABAR_LIMITED", BarcodeType.GS1_DATABAR_LIMITED);
        honeywell2Lib.put("GS1_DATABAR_EXPANDED", BarcodeType.GS1_DATABAR_EXPANDED);
    }

    /**
     * Converts a string returned by one of the ID maps above into one of the available BarcodeTypes.
     * Defaults to the UNKNOWN type if no corresponding type exists.
     * @param symbologyString The string returned by one of the ID maps.
     * @return The corresponding BarcodeType.
     */
    public static BarcodeType toBarcodeType(@Nullable final String symbologyString) {
        if (honeywell2Lib.containsKey(symbologyString)) {
            return honeywell2Lib.get(symbologyString);
        }
        return BarcodeType.UNKNOWN;
    }
}
