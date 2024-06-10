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
        sdk2Api.put(KDCConstants.Symbology.DATA_MATRIX, BarcodeType.DATAMATRIX);
        sdk2Api.put(KDCConstants.Symbology.AUSTRALIAN_POST, BarcodeType.AUS_POST);
        sdk2Api.put(KDCConstants.Symbology.AZTEC_CODE, BarcodeType.AZTEC);
        sdk2Api.put(KDCConstants.Symbology.UPCE, BarcodeType.UPCE);
        sdk2Api.put(KDCConstants.Symbology.GS1128, BarcodeType.GS1_128);
        sdk2Api.put(KDCConstants.Symbology.CODABAR, BarcodeType.CODABAR);
        sdk2Api.put(KDCConstants.Symbology.CODE11, BarcodeType.CODE11);
        sdk2Api.put(KDCConstants.Symbology.CODE93, BarcodeType.CODE93);
        sdk2Api.put(KDCConstants.Symbology.EAN8, BarcodeType.EAN8);
        sdk2Api.put(KDCConstants.Symbology.MSI, BarcodeType.MSI);
        // To be tested
        sdk2Api.put(KDCConstants.Symbology.GS1_OMNI, BarcodeType.GS1_DATABAR);
        sdk2Api.put(KDCConstants.Symbology.GS1_LIMITED, BarcodeType.GS1_DATABAR_LIMITED);
        sdk2Api.put(KDCConstants.Symbology.GS1_EXPANDED, BarcodeType.GS1_DATABAR_EXPANDED);
        sdk2Api.put(KDCConstants.Symbology.UPCA, BarcodeType.UPCA);
        sdk2Api.put(KDCConstants.Symbology.UPCE0, BarcodeType.UPCE);
        sdk2Api.put(KDCConstants.Symbology.UPCE1, BarcodeType.UPCE);
        sdk2Api.put(KDCConstants.Symbology.AZTEC_RUNES, BarcodeType.AZTEC_RUNE);
        sdk2Api.put(KDCConstants.Symbology.MAXICODE, BarcodeType.MAXICODE);
        sdk2Api.put(KDCConstants.Symbology.PDF417, BarcodeType.PDF417);
        sdk2Api.put(KDCConstants.Symbology.QR_CODE, BarcodeType.QRCODE);
        sdk2Api.put(KDCConstants.Symbology.HANXIN, BarcodeType.HAN_XIN);
        sdk2Api.put(KDCConstants.Symbology.BRITISH_POST, BarcodeType.BRITISH_POST);
        sdk2Api.put(KDCConstants.Symbology.CANADIAN_POST, BarcodeType.CANADIAN_POST);
        sdk2Api.put(KDCConstants.Symbology.KIX_POST, BarcodeType.DUTCH_POST);
        sdk2Api.put(KDCConstants.Symbology.JAPANESE_POST, BarcodeType.JAPAN_POST);
        sdk2Api.put(KDCConstants.Symbology.CHINA_POST, BarcodeType.CHINA_POST);
        sdk2Api.put(KDCConstants.Symbology.KOREA_POST, BarcodeType.KOREA_POST);
        sdk2Api.put(KDCConstants.Symbology.GS1_DATABAR, BarcodeType.GS1_DATABAR);
    }

    static BarcodeType sdk2Api(KDCConstants.Symbology symbology) {
        BarcodeType res = sdk2Api.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }
}
