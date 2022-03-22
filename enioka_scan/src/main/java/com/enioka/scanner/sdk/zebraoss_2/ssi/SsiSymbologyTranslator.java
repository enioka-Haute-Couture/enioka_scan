package com.enioka.scanner.sdk.zebraoss_2.ssi;

import com.enioka.scanner.data.BarcodeType;

import java.util.HashMap;
import java.util.Map;

public final class SsiSymbologyTranslator {
    private final static Map<Byte, BarcodeType> sdk2Api = new HashMap<>();

    static {
        sdk2Api.put((byte) 0x03, BarcodeType.CODE128);
        sdk2Api.put((byte) 0x01, BarcodeType.CODE39);
        sdk2Api.put((byte) 0x04, BarcodeType.DIS25);
        sdk2Api.put((byte) 0x06, BarcodeType.INT25);
        sdk2Api.put((byte) 0x0B, BarcodeType.EAN13);
    }

    public static BarcodeType sdk2Api(Byte symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
