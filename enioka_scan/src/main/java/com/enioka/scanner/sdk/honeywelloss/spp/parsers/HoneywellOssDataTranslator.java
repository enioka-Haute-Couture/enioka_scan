package com.enioka.scanner.sdk.honeywelloss.spp.parsers;

import com.enioka.scanner.data.BarcodeType;

import java.util.HashMap;
import java.util.Map;

public class HoneywellOssDataTranslator {
    private final static Map<Byte, BarcodeType> sdk2Api = new HashMap<>();

    static {
        sdk2Api.put((byte) 0x6a, BarcodeType.CODE128);
        sdk2Api.put((byte) 0x62, BarcodeType.CODE39);
        sdk2Api.put((byte) 0x66, BarcodeType.DIS25);
        sdk2Api.put((byte) 0x65, BarcodeType.INT25);
        sdk2Api.put((byte) 0x64, BarcodeType.EAN13);
    }

    public static BarcodeType sdk2Api(Byte symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
