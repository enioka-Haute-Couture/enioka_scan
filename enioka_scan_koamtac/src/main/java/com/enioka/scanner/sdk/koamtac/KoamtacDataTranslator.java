package com.enioka.scanner.sdk.koamtac;

import com.enioka.scanner.data.BarcodeType;

import java.util.HashMap;
import java.util.Map;

import koamtac.kdc.sdk.KDCConstants;

class KoamtacDataTranslator {
    final static Map<KDCConstants.Symbology, BarcodeType> sdk2Api = new HashMap<KDCConstants.Symbology, BarcodeType>();

    static {
        sdk2Api.put(KDCConstants.Symbology.CODE128, BarcodeType.CODE128);
        sdk2Api.put(KDCConstants.Symbology.CODE39, BarcodeType.CODE39);
        sdk2Api.put(KDCConstants.Symbology.S2OF5IND, BarcodeType.DIS25);
        sdk2Api.put(KDCConstants.Symbology.I2OF5, BarcodeType.INT25);
        sdk2Api.put(KDCConstants.Symbology.EAN13, BarcodeType.EAN13);
    }

    static BarcodeType sdk2Api(KDCConstants.Symbology symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
