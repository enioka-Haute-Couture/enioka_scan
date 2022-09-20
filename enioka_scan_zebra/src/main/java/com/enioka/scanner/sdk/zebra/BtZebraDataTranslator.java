package com.enioka.scanner.sdk.zebra;

import com.enioka.scanner.data.BarcodeType;
import com.zebra.scannercontrol.RMDAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class BtZebraDataTranslator {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SYMBOLOGY CODES
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final Map<Integer, BarcodeType> barcodeTypesMapping;

    static {
        // Codes are the SSI ID, found in the SSI Manual in the "DECODE_DATA" packet section
        barcodeTypesMapping = new HashMap<>();
        barcodeTypesMapping.put(1, BarcodeType.CODE39);
        barcodeTypesMapping.put(3, BarcodeType.CODE128);
        barcodeTypesMapping.put(4, BarcodeType.DIS25);
        barcodeTypesMapping.put(6, BarcodeType.INT25);
        barcodeTypesMapping.put(11, BarcodeType.EAN13);
        barcodeTypesMapping.put(28, BarcodeType.QRCODE);
        barcodeTypesMapping.put(45, BarcodeType.AZTEC);

    }

    static BarcodeType sdk2Api(int symbology) {
        BarcodeType res = barcodeTypesMapping.get(symbology);
        if (res == null) {
            return BarcodeType.UNKNOWN;
        }
        return res;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DEFAULT SYMBOLOGIES
    ////////////////////////////////////////////////////////////////////////////////////////////////

    static final ArrayList<Integer> authorizedSymbologies;

    static {
        authorizedSymbologies = new ArrayList<>();
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_39);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_128);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_DISCRETE_2_OF_5);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_INTERLEAVED_2_OF_5);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_EAN_13_JAN_13);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_QR_CODE);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AZTEC);
    }

    static final ArrayList<Integer> unauthorizedSymbologies;

    static {
        unauthorizedSymbologies = new ArrayList<>();
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_A);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_E);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_E_1);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_EAN_8_JAN_8);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_BOOKLAND_EAN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UCC_EAN_128);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_93);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_11);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CHINESE_2_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODABAR);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MSI);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_32);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_DATAMATRIXQR);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_PDF);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISBN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UCC_COUPON_EXTENDED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISSN_EAN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISBT_128);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_TRIOPTIC_CODE_39);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MATRIX_2_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_KOREAN_3_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_14);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_LIMITED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_EXPANDED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MICROPDF417);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MAXICODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_QR_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MICRO_QR_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AZTEC);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_HAN_XIN_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AUSTRALIAN_POST);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_US_PLANET);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_US_POSTNET);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_KIX_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_USPS_4CB);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UK_POSTAL);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_JAPAN_POST);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_FICS);
    }
}
