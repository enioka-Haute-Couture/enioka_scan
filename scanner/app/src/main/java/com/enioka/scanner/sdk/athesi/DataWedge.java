package com.enioka.scanner.sdk.athesi;

/**
 * HHT wrapper layer constants
 */
public class DataWedge {

    //SDC-Decoder
    public static final String ENABLE_SDC_PREVIEW = "ENABLE_SDC_PREVIEW";
    public static final String DISABLE_SDC_PREVIEW = "DISABLE_SDC_PREVIEW";
    public static final String SET_SDC_LICENSE = "SET_SDC_LICENSE";
    public static final String SET_SDC_FOCUSMODE = "SET_SDC_FOCUSMODE";
    public static final String ENABLE_SDC_FLASH = "ENABLE_SDC_FLASH";
    public static final String DISABLE_SDC_FLASH = "DISABLE_SDC_FLASH";
    public static final String PARAM = "PARAM";
    public static final String FOCUS_MODE_AUTO = "auto";
    public static final String FOCUS_MODE_INFINITY = "infinity";
    public static final String FOCUS_MODE_MACRO = "macro";
    public static final String FOCUS_MODE_FIXED = "fixed";
    public static final String FOCUS_MODE_EDOF = "edof";
    public static final String FOCUS_MODE_CONTINUOUS_VIDEO = "continuous-video";

    // action SCANNERINPUTPLUGIN
    public static final String SCANNERINPUTPLUGIN = "com.hht.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
    public static final String DATA_STRING = "com.hht.emdk.datawedge.data_string";
    public static final String DATA_TYPE = "com.hht.emdk.datawedge.data_type";
    public static final String DATA_LENGTH = "com.hht.emdk.datawedge.data_length";

    // action SOFTSCANTRIGGER
    public static final String SOFTSCANTRIGGER = "com.hht.emdk.datawedge.api.ACTION_SOFTSCANTRIGGER";
    public static final String EXTRA_PARAMETER = "com.hht.emdk.datawedge.api.EXTRA_PARAMETER";
    public static final String EXTRA_PARAMETERS = "com.hht.emdk.datawedge.api.EXTRA_PARAMETERS";
    public static final String CODE_PARAMETER = "com.hht.emdk.datawedge.api.CODE_PARAMETER";

    public static final String START_SCANNING = "START_SCANNING";
    public static final String STOP_SCANNING = "STOP_SCANNING";
    public static final String DISABLE_PLUGIN = "DISABLE_PLUGIN";
    public static final String ENABLE_PLUGIN = "ENABLE_PLUGIN";
    public static final String DISABLE_BEEP = "DISABLE_BEEP";
    public static final String ENABLE_BEEP = "ENABLE_BEEP";
    public static final String DISABLE_LED = "DISABLE_LED";
    public static final String ENABLE_LED = "ENABLE_LED";
    public static final String DISABLE_VIBRATE = "DISABLE_VIBRATE";
    public static final String ENABLE_VIBRATE = "ENABLE_VIBRATE";
    public static final String DISABLE_TRIGGERBUTTON = "DISABLE_TRIGGERBUTTON";
    public static final String ENABLE_TRIGGERBUTTON = "ENABLE_TRIGGERBUTTON";

    //Retrieve Setting Information
    public static final String SCANNERSETTING = "SCANNER_SETTING";
    public static final String SET_DEFAULT_SETTING = "SET_DEFAULT_SETTING";

    //CODE 39 FAMILY
    public static final String CODE39_ENABLED = "CODE39_ENABLED";
    public static final String DISABLE_CODE39 = "DISABLE_CODE39";
    public static final String ENABLE_CODE39 = "ENABLE_CODE39";

    public static final String CNVT_CODE39_TO_32_ENABLED = "CNVT_CODE39_TO_32_ENABLED";
    public static final String DISABLE_CNVT_CODE39_TO_32 = "DISABLE_CNVT_CODE39_TO_32";
    public static final String ENABLE_CNVT_CODE39_TO_32 = "ENABLE_CNVT_CODE39_TO_32";

    public static final String CODE32_PREFIX_ENABLED = "CODE32_PREFIX_ENABLED";
    public static final String DISABLE_CODE32_PREFIX = "DISABLE_CODE32_PREFIX";
    public static final String ENABLE_CODE32_PREFIX = "ENABLE_CODE32_PREFIX";

    public static final String CODE39_VER_CHK_DGT_ENABLED = "CODE39_VER_CHK_DGT_ENABLED";
    public static final String DISABLE_CODE39_VER_CHK_DGT = "DISABLE_CODE39_VER_CHK_DGT";
    public static final String ENABLE_CODE39_VER_CHK_DGT = "ENABLE_CODE39_VER_CHK_DGT";

    public static final String CODE39_REPORT_CHK_DGT_ENABLED = "CODE39_REPORT_CHK_DGT_ENABLED";
    public static final String DISABLE_CODE39_REPORT_CHK_DGT = "DISABLE_CODE39_REPORT_CHK_DGT";
    public static final String ENABLE_CODE39_REPORT_CHK_DGT = "ENABLE_CODE39_REPORT_CHK_DGT";

    public static final String CODE39_FULL_ASCII_ENABLED = "CODE39_FULL_ASCII_ENABLED";
    public static final String DISABLE_CODE39_FULL_ASCII = "DISABLE_CODE39_FULL_ASCII";
    public static final String ENABLE_CODE39_FULL_ASCII = "ENABLE_CODE39_FULL_ASCII";

    public static final String DISABLE_TRIOPTIC = "DISABLE_TRIOPTIC";
    public static final String ENABLE_TRIOPTIC = "ENABLE_TRIOPTIC";
    public static final String TRIOPTIC_ENABLED = "TRIOPTIC_ENABLED";

    //CODABAR FAMILY
    public static final String CODABAR_ENABLED = "CODABAR_ENABLED";
    public static final String DISABLE_CODABAR = "DISABLE_CODABAR";
    public static final String ENABLE_CODABAR = "ENABLE_CODABAR";

    public static final String CODABAR_CLSI_ENABLED = "CODABAR_CLSI_ENABLED";
    public static final String DISABLE_CODABAR_CLSI = "DISABLE_CODABAR_CLSI";
    public static final String ENABLE_CODABAR_CLSI = "ENABLE_CODABAR_CLSI";

    public static final String CODABAR_NOTIS_ENABLED = "CODABAR_NOTIS_ENABLED";
    public static final String DISABLE_CODABAR_NOTIS = "DISABLE_CODABAR_NOTIS";
    public static final String ENABLE_CODABAR_NOTIS = "ENABLE_CODABAR_NOTIS";

    //CODE 128 FAMILY
    public static final String DISABLE_CODE128 = "DISABLE_CODE128";
    public static final String ENABLE_CODE128 = "ENABLE_CODE128";
    public static final String CODE128_ENABLED = "CODE128_ENABLED";

    public static final String DISABLE_EAN128 = "DISABLE_EAN128";
    public static final String ENABLE_EAN128 = "ENABLE_EAN128";
    public static final String EAN128_ENABLED = "EAN128_ENABLED";

    public static final String DISABLE_ISBT_128 = "DISABLE_ISBT_128";
    public static final String ENABLE_ISBT_128 = "ENABLE_ISBT_128";
    public static final String ISBT_128_ENABLED = "ISBT_128_ENABLED";

    public static final String DISABLE_ISBT_CONCAT = "DISABLE_ISBT_CONCAT";
    public static final String ENABLE_ISBT_CONCAT = "ENABLE_ISBT_CONCAT";
    public static final String ISBT_CONCAT_ENABLED = "ISBT_CONCAT_ENABLED";

    public static final String DISABLE_ISBT_TABLE = "DISABLE_ISBT_TABLE";
    public static final String ENABLE_ISBT_TABLE = "ENABLE_TABLE_REDUN";
    public static final String ISBT_TABLE_ENABLED = "ISBT_TABLE_ENABLED";

    //CODE 11 FAMILY
    public static final String DISABLE_CODE11 = "DISABLE_CODE11";
    public static final String ENABLE_CODE11 = "ENABLE_CODE11";
    public static final String CODE11_ENABLED = "CODE11_ENABLED";

    public static final String CODE11_VER_CHK_DGT_ENABLED = "CODE11_VER_CHK_DGT_ENABLED";
    public static final String DISABLE_CODE11_VER_CHK_DGT = "DISABLE_CODE11_VER_CHK_DGT";
    public static final String ENABLE_CODE11_VER_CHK_DGT = "ENABLE_CODE11_VER_CHK_DGT";

    public static final String CODE11_REPORT_CHK_DGT_ENABLED = "CODE11_REPORT_CHK_DGT_ENABLED";
    public static final String DISABLE_CODE11_REPORT_CHK_DGT = "DISABLE_CODE11_REPORT_CHK_DGT";
    public static final String ENABLE_CODE11_REPORT_CHK_DGT = "ENABLE_CODE11_REPORT_CHK_DGT";

    // Discrete 2/5
    public static final String DISABLE_D25 = "DISABLE_D25";
    public static final String ENABLE_D25 = "ENABLE_D25";
    public static final String D25_ENABLED = "D25_ENABLED";

    public static final String DISABLE_I25 = "DISABLE_I25";
    public static final String ENABLE_I25 = "ENABLE_I25";
    public static final String I25_ENABLED = "I25_ENABLED";

    public static final String DISABLE_NEC25 = "DISABLE_NEC25";
    public static final String ENABLE_NEC25 = "ENABLE_NEC25";
    public static final String NEC25_ENABLED = "NEC25_ENABLED";

    public static final String DISABLE_S25IATA = "DISABLE_S25IATA";
    public static final String ENABLE_S25IATA = "ENABLE_S25IATA";
    public static final String S25IATA_ENABLED = "S25IATA_ENABLED";

    public static final String DISABLE_S25INDUSTRIAL = "DISABLE_S25INDUSTRIAL";
    public static final String ENABLE_S25INDUSTRIAL = "ENABLE_S25INDUSTRIAL";
    public static final String S25INDUSTRIA_ENABLED = "S25INDUSTRIA_ENABLED";

    public static final String I25_VER_CHK_DGT_ENABLED = "I25_VER_CHK_DGT_ENABLED";
    public static final String DISABLE_I25_VER_CHK_DGT = "DISABLE_I25_VER_CHK_DGT";
    public static final String ENABLE_I25_VER_CHK_DGT = "ENABLE_I25_VER_CHK_DGT";

    public static final String I25_REPORT_CHK_DGT_ENABLED = "I25_REPORT_CHK_DGT_ENABLED";
    public static final String DISABLE_I25_REPORT_CHK_DGT = "DISABLE_I25_REPORT_CHK_DGT";
    public static final String ENABLE_I25_REPORT_CHK_DGT = "ENABLE_I25_REPORT_CHK_DGT";

    public static final String CNVT_I25_TO_EAN13_ENABLED = "CNVT_I25_TO_EAN13_ENABLED";
    public static final String DISABLE_CNVT_I25_TO_EAN13 = "DISABLE_CNVT_I25_TO_EAN13";
    public static final String ENABLE_CNVT_I25_TO_EAN13 = "ENABLE_CNVT_I25_TO_EAN13";

    public static final String DISABLE_CODE93 = "DISABLE_CODE93";
    public static final String ENABLE_CODE93 = "ENABLE_CODE93";
    public static final String CODE93_ENABLED = "CODE93_ENABLED";

    //UPC Family
    public static final String DISABLE_UPCA = "DISABLE_UPCA";
    public static final String ENABLE_UPCA = "ENABLE_UPCA";
    public static final String UPCA_ENABLED = "UPCA_ENABLED";

    public static final String UPCA_REPORT_CHK_DGT_ENABLED = "UPCA_REPORT_CHK_DGT_ENABLED";
    public static final String DISABLE_UPCA_REPORT_CHK_DGT = "DISABLE_UPCA_REPORT_CHK_DGT";
    public static final String ENABLE_UPCA_REPORT_CHK_DGT = "ENABLE_UPCA_REPORT_CHK_DGT";

    public static final String DISABLE_UPCA_PREAMBLE = "DISABLE_UPCA_PREAMBLE";                        //Disable Preamble
    public static final String ENABLE_UPCA_PREAMBLE = "ENABLE_UPCA_PREAMBLE";                        //Enable Preamble - Sys Char ONLY
    public static final String ENABLE_UPCA_PREAMBLE_COUNTSYS = "ENABLE_UPCA_PREAMBLE_COUNTSYS";            //Enable Preamble - Country Code and Sys Char
    public static final String UPCA_PREAMBLE_ENABLED = "UPCA_PREAMBLE_ENABLED";

    public static final String DISABLE_UPCE = "DISABLE_UPCE";
    public static final String ENABLE_UPCE = "ENABLE_UPCE";
    public static final String UPCE_ENABLED = "UPCE_ENABLED";

    public static final String UPCE_REPORT_CHK_DGT_ENABLED = "UPCE_REPORT_CHK_DGT_ENABLED";
    public static final String DISABLE_UPCE_REPORT_CHK_DGT = "DISABLE_UPCE_REPORT_CHK_DGT";
    public static final String ENABLE_UPCE_REPORT_CHK_DGT = "ENABLE_UPCE_REPORT_CHK_DGT";

    public static final String DISABLE_UPCE_PREAMBLE = "DISABLE_UPCE_PREAMBLE";
    public static final String ENABLE_UPCE_PREAMBLE = "ENABLE_UPCE_PREAMBLE";
    public static final String UPCE_PREAMBLE_ENABLED = "UPCE_PREAMBLE_ENABLED";

    public static final String CNVT_UPCE_TO_UPCA_ENABLED = "CNVT_UPCE_TO_UPCA_ENABLED";
    public static final String DISABLE_CNVT_UPCE_TO_UPCA = "DISABLE_CNVT_UPCE_TO_UPCA";
    public static final String ENABLE_CNVT_UPCE_TO_UPCA = "ENABLE_CNVT_UPCE_TO_UPCA";

    public static final String DISABLE_UPCE1 = "DISABLE_UPCE1";
    public static final String ENABLE_UPCE1 = "ENABLE_UPCE1";
    public static final String UPCE1_ENABLED = "UPCE1_ENABLED";

    public static final String UPCE1_REPORT_CHK_DGT_ENABLED = "UPCE1_REPORT_CHK_DGT_ENABLED";
    public static final String DISABLE_UPCE1_REPORT_CHK_DGT = "DISABLE_UPCE1_REPORT_CHK_DGT";
    public static final String ENABLE_UPCE1_REPORT_CHK_DGT = "ENABLE_UPCE1_REPORT_CHK_DGT";

    public static final String DISABLE_UPCE1_PREAMBLE = "DISABLE_UPCE1_PREAMBLE";
    public static final String ENABLE_UPCE1_PREAMBLE = "ENABLE_UPCE1_PREAMBLE";
    public static final String UPCE1_PREAMBLE_ENABLED = "UPCE1_PREAMBLE_ENABLED";

    public static final String CNVT_UPCE1_TO_UPCA_ENABLED = "CNVT_UPCE1_TO_UPCA_ENABLED";
    public static final String DISABLE_CNVT_UPCE1_TO_UPCA = "DISABLE_CNVT_UPCE1_TO_UPCA";
    public static final String ENABLE_CNVT_UPCE1_TO_UPCA = "ENABLE_CNVT_UPCE1_TO_UPCA";

    //EAN Family
    public static final String DISABLE_EAN8 = "DISABLE_EAN8";
    public static final String ENABLE_EAN8 = "ENABLE_EAN8";
    public static final String EAN8_ENABLED = "EAN8_ENABLED";

    public static final String DISABLE_EAN8_ZEROEXTEND = "DISABLE_EAN8_ZEROEXTEND";
    public static final String ENABLE_EAN8_ZEROEXTEND = "ENABLE_EAN8_ZEROEXTEND";
    public static final String EAN8_ZEROEXTEND_ENABLED = "EAN8_ZEROEXTEND_ENABLED";

    public static final String DISABLE_EAN13 = "DISABLE_EAN13";
    public static final String ENABLE_EAN13 = "ENABLE_EAN13";
    public static final String EAN13_ENABLED = "EAN13_ENABLED";

    public static final String DISABLE_EAN13_SUPP = "DISABLE_EAN13_SUPP";
    public static final String ENABLE_EAN13_SUPP = "ENABLE_EAN13_SUPP";

    public static final String DISABLE_BOOKLAND_ISBN = "DISABLE_BOOKLAND_ISBN";
    public static final String ENABLE_BOOKLAND_ISBN = "ENABLE_BOOKLAND_ISBN";
    public static final String BOOKLAND_ISBN_ENABLED = "BOOKLAND_ISBN_ENABLED";

    public static final String DISABLE_BOOKLAND_EAN = "DISABLE_BOOKLAND_EAN";
    public static final String ENABLE_BOOKLAND_EAN = "ENABLE_BOOKLAND_EAN";
    public static final String BOOKLAND_EAN_ENABLED = "BOOKLAND_EAN_ENABLED";

    public static final String DISABLE_UCC_EXT_CODE = "DISABLE_UCC_EXT_CODE";
    public static final String ENABLE_UCC_EXT_CODE = "ENABLE_UCC_EXT_CODE";
    public static final String UCC_EXT_CODE_ENABLED = "UCC_EXT_CODE_ENABLED";

    public static final String DISABLE_ISSN_EAN = "DISABLE_ISSN_EAN";
    public static final String ENABLE_ISSN_EAN = "ENABLE_ISSN_EAN";
    public static final String ISSN_EAN_ENABLED = "ISSN_EAN_ENABLED";

    public static final String DISABLE_MSI = "DISABLE_MSI";
    public static final String ENABLE_MSI = "ENABLE_MSI";
    public static final String MSI_ENABLED = "MSI_ENABLED";

    public static final String DISABLE_MSI_REPORT_CHK_DGT = "DISABLE_MSI_REPORT_CHK_DGT";
    public static final String ENABLE_MSI_REPORT_CHK_DGT = "ENABLE_MSI_REPORT_CHK_DGT";
    public static final String MSI_REPORT_CHK_DGT_ENABLED = "MSI_REPORT_CHK_DGT_ENABLED";


    // Reduced Space Symbology(RSS), GS1 DataBar
    public static final String DISABLE_RSS_14 = "DISABLE_RSS_14";
    public static final String ENABLE_RSS_14 = "ENABLE_RSS_14";
    public static final String RSS_14_ENABLED = "RSS_14_ENABLED";

    public static final String DISABLE_RSS_LIM = "DISABLE_RSS_LIM";
    public static final String ENABLE_RSS_LIM = "ENABLE_RSS_LIM";
    public static final String RSS_LIM_ENABLED = "RSS_LIM_ENABLED";

    public static final String DISABLE_RSS_EXP = "DISABLE_RSS_EXP";
    public static final String ENABLE_RSS_EXP = "ENABLE_RSS_EXP";
    public static final String RSS_EXP_ENABLED = "RSS_EXP_ENABLED";

    public static final String DISABLE_RSS_TO_UPC = "DISABLE_RSS_TO_UPC";
    public static final String ENABLE_RSS_TO_UPC = "ENABLE_RSS_TO_UPC";
    public static final String RSS_TO_UPC_ENABLED = "RSS_TO_UPC_ENABLED";

    // composite
    public static final String DISABLE_COMPOSITE_CCC = "DISABLE_COMPOSITE_CCC";
    public static final String ENABLE_COMPOSITE_CCC = "ENABLE_COMPOSITE_CCC";
    public static final String COMPOSITE_CCC_ENABLED = "COMPOSITE_CCC_ENABLED";

    public static final String DISABLE_COMPOSITE_CCAB = "DISABLE_COMPOSITE_CCAB";
    public static final String ENABLE_COMPOSITE_CCAB = "ENABLE_COMPOSITE_CCAB";
    public static final String COMPOSITE_CCAB_ENABLED = "COMPOSITE_CCAB_ENABLED";

    public static final String DISABLE_COMPOSITE_TLC39 = "DISABLE_COMPOSITE_TLC39";
    public static final String ENABLE_COMPOSITE_TLC39 = "ENABLE_COMPOSITE_TLC39";
    public static final String COMPOSITE_TLC39_ENABLED = "COMPOSITE_TLC39_ENABLED";

    public static final String DISABLE_COMPOSITE_RSS = "DISABLE_COMPOSITE_RSS";
    public static final String ENABLE_COMPOSITE_RSS = "ENABLE_COMPOSITE_RSS";
    public static final String COMPOSITE_RSS_ENABLED = "COMPOSITE_RSS_ENABLED";

    //CHINESE 25
    public static final String DISABLE_CHINA = "DISABLE_CHINA";
    public static final String ENABLE_CHINA = "ENABLE_CHINA";
    public static final String CHINA_ENABLED = "CHINA_ENABLED";

    ///
    public static final String DISABLE_KOREAN35 = "DISABLE_KOREAN35";
    public static final String ENABLE_KOREAN35 = "ENABLE_KOREAN35";
    public static final String KOREAN35_ENABLED = "KOREAN35_ENABLED";

    public static final String DISABLE_MATRIX25 = "DISABLE_MATRIX25";
    public static final String ENABLE_MATRIX25 = "ENABLE_MATRIX25";
    public static final String MATRIX25_ENABLED = "MATRIX25_ENABLED";

    public static final String DISABLE_MATRIX25_REDUN = "DISABLE_MATRIX25_REDUN";
    public static final String ENABLE_MATRIX25_REDUN = "ENABLE_MATRIX25_REDUN";
    public static final String MATRIX25_REDUN_ENABLED = "MATRIX25_REDUN_ENABLED";

    public static final String DISABLE_MATRIX25_VER_CHK_DGT = "DISABLE_MATRIX25_VER_CHK_DGT";
    public static final String ENABLE_MATRIX25_VER_CHK_DGT = "ENABLE_MATRIX25_VER_CHK_DGT";
    public static final String MATRIX25_VER_CHK_DGT_ENABLED = "MATRIX25_VER_CHK_DGT_ENABLED";

    public static final String DISABLE_MATRIX25_CHK_DGT = "DISABLE_MATRIX25_CHK_DGT";
    public static final String ENABLE_MATRIX25_CHK_DGT = "ENABLE_MATRIX25_CHK_DGT";
    public static final String MATRIX25_CHK_DGT_ENABLED = "MATRIX25_CHK_DGT_ENABLED";

    // Postal
    public static final String DISABLE_US_POSTNET = "DISABLE_US_POSTNET";
    public static final String ENABLE_US_POSTNET = "ENABLE_US_POSTNET";
    public static final String US_POSTNET_ENABLED = "US_POSTNET_ENABLED";

    public static final String DISABLE_US_PLANET = "DISABLE_US_PLANET";
    public static final String ENABLE_US_PLANET = "ENABLE_US_PLANET";
    public static final String US_PLANET_ENABLED = "US_PLANET_ENABLED";

    public static final String DISABLE_US_POSTAL_CHK_DGT = "DISABLE_US_POSTAL_CHK_DGT";
    public static final String ENABLE_US_POSTAL_CHK_DGT = "ENABLE_US_POSTAL_CHK_DGT";
    public static final String US_POSTAL_CHK_DGT_ENABLED = "US_POSTAL_CHK_DGT_ENABLED";

    public static final String DISABLE_UK_POSTAL = "DISABLE_UK_POSTAL";
    public static final String ENABLE_UK_POSTAL = "ENABLE_UK_POSTAL";
    public static final String UK_POSTAL_ENABLED = "UK_POSTAL_ENABLED";

    public static final String DISABLE_UK_POSTAL_CHK_DGT = "DISABLE_UK_POSTAL_CHK_DGT";
    public static final String ENABLE_UK_POSTAL_CHK_DGT = "ENABLE_UK_POSTAL_CHK_DGT";
    public static final String UK_POSTAL_CHK_DGT_ENABLED = "UK_POSTAL_CHK_DGT_ENABLED";

    public static final String DISABLE_JAPAN_POSTAL = "DISABLE_JAPAN_POSTAL";
    public static final String ENABLE_JAPAN_POSTAL = "ENABLE_JAPAN_POSTAL";
    public static final String JAPAN_POSTAL_ENABLED = "JAPAN_POSTAL_ENABLED";

    public static final String DISABLE_AUSTRALIA_POST = "DISABLE_AUSTRALIA_POST";
    public static final String ENABLE_AUSTRALIA_POST = "ENABLE_AUSTRALIA_POST";
    public static final String AUSTRALIA_POST_ENABLED = "AUSTRALIA_POST_ENABLED";

    public static final String DISABLE_KIX_CODE = "DISABLE_KIX_CODE";
    public static final String ENABLE_KIX_CODE = "ENABLE_KIX_CODE";
    public static final String KIX_CODE_ENABLED = "KIX_CODE_ENABLED";

    public static final String DISABLE_ONE_CODE = "DISABLE_ONE_CODE";
    public static final String ENABLE_ONE_CODE = "ENABLE_ONE_CODE";
    public static final String ONE_CODE_ENABLED = "ONE_CODE_ENABLED";

    public static final String DISABLE_UPU_FICS_POSTAL = "DISABLE_UPU_FICS_POSTAL";
    public static final String ENABLE_UPU_FICS_POSTAL = "ENABLE_UPU_FICS_POSTAL";
    public static final String UPU_FICS_POSTAL_ENABLED = "UPU_FICS_POSTAL_ENABLED";

    ///
    // SE4500
    // 2D Barcode
    public static final String DISABLE_PDF417 = "DISABLE_PDF417";
    public static final String ENABLE_PDF417 = "ENABLE_PDF417";
    public static final String PDF417_ENABLED = "PDF417_ENABLED";

    public static final String DISABLE_MICROPDF417 = "DISABLE_MICROPDF417";
    public static final String ENABLE_MICROPDF417 = "ENABLE_MICROPDF417";
    public static final String MICROPDF417_ENABLED = "MICROPDF417_ENABLED";

    public static final String DISABLE_CODE128EML = "DISABLE_CODE128EML";
    public static final String ENABLE_CODE128EML = "ENABLE_CODE128EML";
    public static final String CODE128EML_ENABLED = "CODE128EML_ENABLED";

    public static final String DISABLE_DATAMATRIX = "DISABLE_DATAMATRIX";
    public static final String ENABLE_DATAMATRIX = "ENABLE_DATAMATRIX";
    public static final String DATAMATRIX_ENABLED = "DATAMATRIX_ENABLED";

    public static final String DISABLE_MAXICODE = "DISABLE_MAXICODE";
    public static final String ENABLE_MAXICODE = "ENABLE_MAXICODE";
    public static final String MAXICODE_ENABLED = "MAXICODE_ENABLED";

    public static final String DISABLE_QRCODE = "DISABLE_QRCODE";
    public static final String ENABLE_QRCODE = "ENABLE_QRCODE";
    public static final String QRCODE_ENABLED = "QRCODE_ENABLED";

    public static final String DISABLE_MICROQR = "DISABLE_MICROQR";
    public static final String ENABLE_MICROQR = "ENABLE_MICROQR";
    public static final String MICROQR_ENABLED = "MICROQR_ENABLED";

    public static final String DISABLE_AZTEC = "DISABLE_AZTEC";
    public static final String ENABLE_AZTEC = "ENABLE_AZTEC";
    public static final String AZTEC_ENABLED = "AZTEC_ENABLED";

    public static final String DISABLE_HAN_XIN = "DISABLE_HAN_XIN";
    public static final String ENABLE_HAN_XIN = "ENABLE_HAN_XIN";
    public static final String HAN_XIN_ENABLED = "HAN_XIN_ENABLED";


    ///
    // Misc added later to datawedge.
    public static final String DISABLE_JAPAN = "DISABLE_JAPAN";
    public static final String ENABLE_JAPAN = "ENABLE_JAPAN";
    public static final String JAPAN_ENABLED = "JAPAN_ENABLED";

    public static final String DISABLE_KIXCODE = "DISABLE_KIXCODE";
    public static final String ENABLE_KIXCODE = "ENABLE_KIXCODE";
    public static final String KIXCODE_ENABLED = "KIXCODE_ENABLED";

    public static final String DISABLE_RSS_AUSTRALIA = "DISABLE_RSS_AUSTRALIA";
    public static final String ENABLE_RSS_AUSTRALIA = "ENABLE_RSS_AUSTRALIA";
    public static final String RSS_AUSTRALIA_ENABLED = "RSS_AUSTRALIA_ENABLED";
}
