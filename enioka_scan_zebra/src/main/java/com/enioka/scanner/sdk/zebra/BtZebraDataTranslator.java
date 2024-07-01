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
        barcodeTypesMapping.put(0x01, BarcodeType.CODE39);
        barcodeTypesMapping.put(0x02, BarcodeType.CODABAR);
        barcodeTypesMapping.put(0x03, BarcodeType.CODE128);
        barcodeTypesMapping.put(0x04, BarcodeType.DIS25);
        barcodeTypesMapping.put(0x06, BarcodeType.INT25);
        barcodeTypesMapping.put(0x07, BarcodeType.CODE93);
        barcodeTypesMapping.put(0x08, BarcodeType.UPCA);
        barcodeTypesMapping.put(0x09, BarcodeType.UPCE);
        barcodeTypesMapping.put(0x0A, BarcodeType.EAN8);
        barcodeTypesMapping.put(0x0B, BarcodeType.EAN13);
        barcodeTypesMapping.put(0x0C, BarcodeType.CODE11);
        barcodeTypesMapping.put(0x0E, BarcodeType.MSI);
        barcodeTypesMapping.put(0x0F, BarcodeType.GS1_128);
        barcodeTypesMapping.put(0x10, BarcodeType.UPCE);
        barcodeTypesMapping.put(0x11, BarcodeType.PDF417);
        barcodeTypesMapping.put(0x13, BarcodeType.CODE39_FULL_ASCII);
        barcodeTypesMapping.put(0x1B, BarcodeType.DATAMATRIX);
        barcodeTypesMapping.put(0x1C, BarcodeType.QRCODE);
        barcodeTypesMapping.put(0x22, BarcodeType.JAPAN_POST);
        barcodeTypesMapping.put(0x23, BarcodeType.AUS_POST);
        barcodeTypesMapping.put(0x24, BarcodeType.DUTCH_POST);
        barcodeTypesMapping.put(0x25, BarcodeType.MAXICODE);
        barcodeTypesMapping.put(0x26, BarcodeType.CANADIAN_POST);
        barcodeTypesMapping.put(0x27, BarcodeType.BRITISH_POST);
        barcodeTypesMapping.put(0x2D, BarcodeType.AZTEC);
        barcodeTypesMapping.put(0x2E, BarcodeType.AZTEC_RUNE);
        barcodeTypesMapping.put(0x30, BarcodeType.GS1_DATABAR);
        barcodeTypesMapping.put(0x31, BarcodeType.GS1_DATABAR_LIMITED);
        barcodeTypesMapping.put(0x32, BarcodeType.GS1_DATABAR_EXPANDED);
        barcodeTypesMapping.put(0x48, BarcodeType.UPCA);
        barcodeTypesMapping.put(0x49, BarcodeType.UPCE);
        barcodeTypesMapping.put(0x50, BarcodeType.UPCE);
        barcodeTypesMapping.put(0x72, BarcodeType.CHINA_POST);
        barcodeTypesMapping.put(0x88, BarcodeType.UPCA);
        barcodeTypesMapping.put(0x89, BarcodeType.UPCE);
        barcodeTypesMapping.put(0x8A, BarcodeType.EAN8);
        barcodeTypesMapping.put(0x8B, BarcodeType.EAN13);
        barcodeTypesMapping.put(0xB4, BarcodeType.GS1_DATABAR_EXPANDED);
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
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_11);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_93);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UK_POSTAL);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_JAPAN_POST);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODABAR);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_A);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_E);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_EAN_8_JAN_8);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_DATAMATRIXQR);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MAXICODE);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_EXPANDED);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_14);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_GS1_DATABAR_LIMITED);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MSI);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_PDF);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AZTEC);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_KIX_CODE);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_KOREAN_3_OF_5);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_AUSTRALIAN_POST);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CHINESE_2_OF_5);
        authorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_HAN_XIN_CODE);
    }

    static final ArrayList<Integer> unauthorizedSymbologies;

    static {
        unauthorizedSymbologies = new ArrayList<>();
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UPC_E_1);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_BOOKLAND_EAN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UCC_EAN_128);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_CODE_32);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISBN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_UCC_COUPON_EXTENDED);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISSN_EAN);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_ISBT_128);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_TRIOPTIC_CODE_39);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MATRIX_2_OF_5);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MICROPDF417);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_QR_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_MICRO_QR_CODE);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_US_PLANET);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_US_POSTNET);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_USPS_4CB);
        unauthorizedSymbologies.add(RMDAttributes.RMD_ATTR_SYM_FICS);
    }
}
