package com.enioka.scanner.sdk.zebraoss.ssi;

import com.enioka.scanner.data.BarcodeType;

import java.util.HashMap;
import java.util.Map;

public final class SsiSymbologyTranslator {
    private final static Map<Byte, BarcodeType> sdk2Api = new HashMap<>();

    static {
        // Codes are the SSI ID, found in the SSI Manual in the "DECODE_DATA" packet section
        sdk2Api.put((byte) 0x03, BarcodeType.CODE128);
        sdk2Api.put((byte) 0x01, BarcodeType.CODE39);
        sdk2Api.put((byte) 0x04, BarcodeType.DIS25);
        sdk2Api.put((byte) 0x06, BarcodeType.INT25);
        sdk2Api.put((byte) 0x0B, BarcodeType.EAN13);
        sdk2Api.put((byte) 0x1C, BarcodeType.QRCODE);
        sdk2Api.put((byte) 0x2D, BarcodeType.AZTEC);
        sdk2Api.put((byte) 0x02, BarcodeType.CODABAR);
        sdk2Api.put((byte) 0x07, BarcodeType.CODE93);
        sdk2Api.put((byte) 0x08, BarcodeType.UPCA);
        sdk2Api.put((byte) 0x09, BarcodeType.UPCE);
        sdk2Api.put((byte) 0x0A, BarcodeType.EAN8);
        sdk2Api.put((byte) 0x0C, BarcodeType.CODE11);
        sdk2Api.put((byte) 0x0E, BarcodeType.MSI);
        sdk2Api.put((byte) 0x0F, BarcodeType.GS1_128);
        sdk2Api.put((byte) 0x11, BarcodeType.PDF417);
        sdk2Api.put((byte) 0x13, BarcodeType.CODE39_FULL_ASCII);
        sdk2Api.put((byte) 0x16, BarcodeType.EAN13);
        // To be tested
        sdk2Api.put((byte) 0x18, BarcodeType.CODABAR);
        // To be tested
        sdk2Api.put((byte) 0x19, BarcodeType.CODE128);
        sdk2Api.put((byte) 0x1B, BarcodeType.DATAMATRIX);
        // To be tested
        sdk2Api.put((byte) 0x21, BarcodeType.CODE128);
        sdk2Api.put((byte) 0x22, BarcodeType.JAPAN_POST);
        sdk2Api.put((byte) 0x23, BarcodeType.AUS_POST);
        sdk2Api.put((byte) 0x24, BarcodeType.DUTCH_POST);
        sdk2Api.put((byte) 0x25, BarcodeType.MAXICODE);
        sdk2Api.put((byte) 0x26, BarcodeType.CANADIAN_POST);
        sdk2Api.put((byte) 0x27, BarcodeType.BRITISH_POST);
        sdk2Api.put((byte) 0x2E, BarcodeType.AZTEC_RUNE);
        // Te be tested
        sdk2Api.put((byte) 0x30, BarcodeType.GS1_DATABAR);
        sdk2Api.put((byte) 0x31, BarcodeType.GS1_DATABAR_LIMITED);
        sdk2Api.put((byte) 0x32, BarcodeType.GS1_DATABAR_EXPANDED);
        // To be tested
        sdk2Api.put((byte) 0x73, BarcodeType.KOREA_POST);
        // To be tested
        sdk2Api.put((byte) 0xB4, BarcodeType.GS1_DATABAR);
    }

    public static BarcodeType sdk2Api(Byte symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
