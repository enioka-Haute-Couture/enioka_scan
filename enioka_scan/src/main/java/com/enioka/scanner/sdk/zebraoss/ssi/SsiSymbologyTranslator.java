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
    }

    public static BarcodeType sdk2Api(Byte symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
