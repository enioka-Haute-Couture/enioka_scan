package com.enioka.scanner.sdk.athesi;

import com.enioka.scanner.data.BarcodeType;

/**
 * Index for all HHT symbologies. Key name is the name returned by the scanner
 */
enum HHTSymbology {
    // Items supported by the lib
    CODE39(DataWedge.ENABLE_CODE39, DataWedge.DISABLE_CODE39, BarcodeType.CODE39, 1),
    CODE128(DataWedge.ENABLE_CODE128, DataWedge.DISABLE_CODE128, BarcodeType.CODE128, 3),
    DIS25(DataWedge.ENABLE_D25, DataWedge.DISABLE_D25, BarcodeType.DIS25, 4),
    INT25(DataWedge.ENABLE_I25, DataWedge.DISABLE_I25, BarcodeType.INT25, 6),
    EAN13(DataWedge.ENABLE_EAN13, DataWedge.DISABLE_EAN13, BarcodeType.EAN13, 11),

    // Items not supported by the lib. Present because we need to be able to disable them.
    CNVT_CODE39_TO_32(DataWedge.ENABLE_CNVT_CODE39_TO_32, DataWedge.DISABLE_CNVT_CODE39_TO_32, null, null),
    CODE32(DataWedge.ENABLE_CODE32_PREFIX, DataWedge.DISABLE_CODE32_PREFIX, null, null),
    CODE39_VER_CHK_DGT(DataWedge.ENABLE_CODE39_VER_CHK_DGT, DataWedge.DISABLE_CODE39_VER_CHK_DGT, null, null),
    CODE39_REPORT_CHK_DGT(DataWedge.ENABLE_CODE39_REPORT_CHK_DGT, DataWedge.DISABLE_CODE39_REPORT_CHK_DGT, null, null),
    CODE39_FULL_ASCII(DataWedge.ENABLE_CODE39_FULL_ASCII, DataWedge.DISABLE_CODE39_FULL_ASCII, null, null),
    TRIOPTIC(DataWedge.ENABLE_TRIOPTIC, DataWedge.DISABLE_TRIOPTIC, null, null),
    CODABAR(DataWedge.ENABLE_CODABAR, DataWedge.DISABLE_CODABAR, null, null),
    CODABAR_CLSI(DataWedge.ENABLE_CODABAR_CLSI, DataWedge.DISABLE_CODABAR_CLSI, null, null),
    CODABAR_NOTIS(DataWedge.ENABLE_CODABAR_NOTIS, DataWedge.DISABLE_CODABAR_NOTIS, null, null),
    EAN128(DataWedge.ENABLE_EAN128, DataWedge.DISABLE_EAN128, null, null),
    ISBT_128(DataWedge.ENABLE_ISBT_128, DataWedge.DISABLE_ISBT_128, null, null),
    ISBT_CONCAT(DataWedge.ENABLE_ISBT_CONCAT, DataWedge.DISABLE_ISBT_CONCAT, null, null),
    ISBT_TABLE(DataWedge.ENABLE_ISBT_TABLE, DataWedge.DISABLE_ISBT_TABLE, null, null),
    CODE11(DataWedge.ENABLE_CODE11, DataWedge.DISABLE_CODE11, null, null),
    CODE11_VER_CHK_DGT(DataWedge.ENABLE_CODE11_VER_CHK_DGT, DataWedge.DISABLE_CODE11_VER_CHK_DGT, null, null),
    CODE11_REPORT_CHK_DGT(DataWedge.ENABLE_CODE11_REPORT_CHK_DGT, DataWedge.DISABLE_CODE11_REPORT_CHK_DGT, null, null),
    NEC25(DataWedge.ENABLE_NEC25, DataWedge.DISABLE_NEC25, null, null),
    S25IATA(DataWedge.ENABLE_S25IATA, DataWedge.DISABLE_S25IATA, null, null),
    S25INDUSTRIAL(DataWedge.ENABLE_S25INDUSTRIAL, DataWedge.DISABLE_S25INDUSTRIAL, null, null),
    I25_VER_CHK_DGT(DataWedge.ENABLE_I25_VER_CHK_DGT, DataWedge.DISABLE_I25_VER_CHK_DGT, null, null),
    I25_REPORT_CHK_DGT(DataWedge.ENABLE_I25_REPORT_CHK_DGT, DataWedge.DISABLE_I25_REPORT_CHK_DGT, null, null),
    CNVT_I25_TO_EAN13(DataWedge.ENABLE_CNVT_I25_TO_EAN13, DataWedge.DISABLE_CNVT_I25_TO_EAN13, null, null),
    CODE93(DataWedge.ENABLE_CODE93, DataWedge.DISABLE_CODE93, null, null),

    BOOKLAND_ISBN(DataWedge.ENABLE_BOOKLAND_ISBN, DataWedge.DISABLE_BOOKLAND_ISBN, null, null, "Scanner_BOOKLANDISBN"),
    ISSN_EAN(DataWedge.ENABLE_ISSN_EAN, DataWedge.DISABLE_ISSN_EAN, null, null, "Scanner_ISSNEAN"),
    UPCA_PREAMBLE_COUNTSYS(DataWedge.ENABLE_UPCA_PREAMBLE_COUNTSYS, "DISABLE_UPCA_PREAMBLE_COUNTSYS", null, null, "Scanner_UPCA_PREAMBLE_COUNTRY_SYS");

    //TODO: finish the list. Items not in list are not disabled on startup.

    public String activation, deactivation, prmPropertyName;
    public BarcodeType type;
    public Integer hhtTypeEnum;

    HHTSymbology(String activationPrm, String deactivationPrm, BarcodeType type, Integer hhtTypeEnum) {
        this(activationPrm, deactivationPrm, type, hhtTypeEnum, activationPrm.replace("ENABLE_", "Scanner_"));
    }

    HHTSymbology(String activationPrm, String deactivationPrm, BarcodeType type, Integer hhtTypeEnum, String prmCode) {
        this.activation = activationPrm;
        this.deactivation = deactivationPrm;
        this.type = type;
        this.hhtTypeEnum = hhtTypeEnum;
        this.prmPropertyName = prmCode;
    }

    public static HHTSymbology getSymbology(BarcodeType type) {
        for (HHTSymbology s : HHTSymbology.values()) {
            if (type.equals(s.type)) {
                return s;
            }
        }
        return null;
    }

    public static HHTSymbology getSymbology(Integer hhtTypeIndex) {
        for (HHTSymbology s : HHTSymbology.values()) {
            if (hhtTypeIndex.equals(s.hhtTypeEnum)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Get a symbology from the name specified in the HHT configuration.
     *
     * @param prmPropertyName name from the HHT configuration.
     * @return the symbology or null if not found.
     */
    public static HHTSymbology getSymbology(String prmPropertyName) {
        for (HHTSymbology s : HHTSymbology.values()) {
            if (prmPropertyName.equals(s.prmPropertyName)) {
                return s;
            }
        }
        return null;
    }
}
