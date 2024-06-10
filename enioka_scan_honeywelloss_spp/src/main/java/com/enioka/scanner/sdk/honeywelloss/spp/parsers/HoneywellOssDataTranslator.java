package com.enioka.scanner.sdk.honeywelloss.spp.parsers;

import com.enioka.scanner.data.BarcodeType;
import com.enioka.scanner.sdk.honeywelloss.spp.SymbologyId;

import java.util.HashMap;
import java.util.Map;

public class HoneywellOssDataTranslator {
    private final static Map<Byte, BarcodeType> sdk2Api = new HashMap<>();

    static {
        for (Integer key: SymbologyId.honeywellIdMap.keySet()) {
            sdk2Api.put(key.byteValue(), SymbologyId.toBarcodeType(SymbologyId.honeywellIdMap.get(key)));
        }
    }

    public static BarcodeType sdk2Api(Byte symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
