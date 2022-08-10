package com.enioka.scanner.sdk.athesi.SPA43LTE;

import com.enioka.scanner.data.BarcodeType;

/**
 * Index for all HHT symbologies. Key name is the name returned by the scanner
 */
enum AthesiHHTSymbology {
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

    UPCA(DataWedge.ENABLE_UPCA, DataWedge.DISABLE_UPCA, null, null),
    UPCA_PREAMBLE(DataWedge.ENABLE_UPCA_PREAMBLE, DataWedge.DISABLE_UPCA_PREAMBLE, null, null),
    UPCE(DataWedge.ENABLE_UPCE, DataWedge.DISABLE_UPCE, null, null),
    UPCE1(DataWedge.ENABLE_UPCE1, DataWedge.DISABLE_UPCE1, null, null),
    EAN8(DataWedge.ENABLE_EAN8, DataWedge.DISABLE_EAN8, null, null),
    BOOKLAND_EAN(DataWedge.ENABLE_BOOKLAND_EAN, DataWedge.DISABLE_BOOKLAND_EAN, null, null),
    UCC_EXT_CODE(DataWedge.ENABLE_UCC_EXT_CODE, DataWedge.DISABLE_UCC_EXT_CODE, null, null),
    MSI(DataWedge.ENABLE_MSI, DataWedge.DISABLE_MSI, null, null),
    RSS_14(DataWedge.ENABLE_RSS_14, DataWedge.DISABLE_RSS_14, null, null),
    CHINA(DataWedge.ENABLE_CHINA, DataWedge.DISABLE_CHINA, null, null),
    KOREAN35(DataWedge.ENABLE_KOREAN35, DataWedge.DISABLE_KOREAN35, null, null),
    MATRIX25(DataWedge.ENABLE_MATRIX25, DataWedge.DISABLE_MATRIX25, null, null),

    US_POSTNET(DataWedge.ENABLE_US_POSTNET, DataWedge.DISABLE_US_POSTNET, null, null),
    US_PLANET(DataWedge.ENABLE_US_PLANET, DataWedge.DISABLE_US_PLANET, null, null),
    UK_POSTAL(DataWedge.ENABLE_UK_POSTAL, DataWedge.DISABLE_UK_POSTAL, null, null),
    JAPAN_POSTAL(DataWedge.ENABLE_JAPAN_POSTAL, DataWedge.DISABLE_JAPAN_POSTAL, null, null),
    AUSTRALIA_POST(DataWedge.ENABLE_AUSTRALIA_POST, DataWedge.DISABLE_AUSTRALIA_POST, null, null),
    KIX_CODE(DataWedge.ENABLE_KIX_CODE, DataWedge.DISABLE_KIX_CODE, null, null),
    ONE_CODE(DataWedge.ENABLE_ONE_CODE, DataWedge.DISABLE_ONE_CODE, null, null),
    UPU_FICS_POSTAL(DataWedge.ENABLE_UPU_FICS_POSTAL, DataWedge.DISABLE_UPU_FICS_POSTAL, null, null),

    PDF417(DataWedge.ENABLE_PDF417, DataWedge.DISABLE_PDF417, null, null),
    MICROPDF417(DataWedge.ENABLE_MICROPDF417, DataWedge.DISABLE_MICROPDF417, null, null),
    CODE128EML(DataWedge.ENABLE_CODE128EML, DataWedge.DISABLE_CODE128EML, null, null),
    DATAMATRIX(DataWedge.ENABLE_DATAMATRIX, DataWedge.DISABLE_DATAMATRIX, null, null),
    MAXICODE(DataWedge.ENABLE_MAXICODE, DataWedge.DISABLE_MAXICODE, null, null),
    QRCODE(DataWedge.ENABLE_QRCODE, DataWedge.DISABLE_QRCODE, BarcodeType.QRCODE, 28),
    MICROQR(DataWedge.ENABLE_MICROQR, DataWedge.DISABLE_MICROQR, null, null),
    AZTEC(DataWedge.ENABLE_AZTEC, DataWedge.DISABLE_AZTEC, BarcodeType.AZTEC, 45),
    HAN_XIN(DataWedge.ENABLE_HAN_XIN, DataWedge.DISABLE_HAN_XIN, null, null),

    JAPAN(DataWedge.ENABLE_JAPAN, DataWedge.DISABLE_JAPAN, null, null),
    KIXCODE(DataWedge.ENABLE_KIXCODE, DataWedge.DISABLE_KIXCODE, null, null),
    RSS_AUSTRALIA(DataWedge.ENABLE_RSS_AUSTRALIA, DataWedge.DISABLE_RSS_AUSTRALIA, null, null),

    BOOKLAND_ISBN(DataWedge.ENABLE_BOOKLAND_ISBN, DataWedge.DISABLE_BOOKLAND_ISBN, null, null, "Scanner_BOOKLANDISBN"),
    ISSN_EAN(DataWedge.ENABLE_ISSN_EAN, DataWedge.DISABLE_ISSN_EAN, null, null, "Scanner_ISSNEAN"),
    UPCA_PREAMBLE_COUNTSYS(DataWedge.ENABLE_UPCA_PREAMBLE_COUNTSYS, "DISABLE_UPCA_PREAMBLE_COUNTSYS", null, null, "Scanner_UPCA_PREAMBLE_COUNTRY_SYS");

    public String activation, deactivation, prmPropertyName;
    public BarcodeType type;
    public Integer hhtTypeEnum;

    AthesiHHTSymbology(String activationPrm, String deactivationPrm, BarcodeType type, Integer hhtTypeEnum) {
        this(activationPrm, deactivationPrm, type, hhtTypeEnum, activationPrm.replace("ENABLE_", "Scanner_"));
    }

    AthesiHHTSymbology(String activationPrm, String deactivationPrm, BarcodeType type, Integer hhtTypeEnum, String prmCode) {
        this.activation = activationPrm;
        this.deactivation = deactivationPrm;
        this.type = type;
        this.hhtTypeEnum = hhtTypeEnum;
        this.prmPropertyName = prmCode;
    }

    public static AthesiHHTSymbology getSymbology(BarcodeType type) {
        for (AthesiHHTSymbology s : AthesiHHTSymbology.values()) {
            if (type.equals(s.type)) {
                return s;
            }
        }
        return null;
    }

    public static AthesiHHTSymbology getSymbology(Integer hhtTypeIndex) {
        for (AthesiHHTSymbology s : AthesiHHTSymbology.values()) {
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
    public static AthesiHHTSymbology getSymbology(String prmPropertyName) {
        for (AthesiHHTSymbology s : AthesiHHTSymbology.values()) {
            if (prmPropertyName.equals(s.prmPropertyName)) {
                return s;
            }
        }
        return null;
    }
}
