package com.enioka.scanner.sdk.zebra.dw;

public class ZebraDwIntents {
    static final String DW_MAIN_CALLBACK_ACTION = "com.dw.ACT";
    static final String DW_CONFIGURATION_CALLBACK_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
    static final String DW_STATUS_CALLBACK_ACTION = "com.symbol.datawedge.scanner_status";
    static final String DW_API_MAIN_ACTION = "com.symbol.datawedge.api.ACTION";
    static final String DW_NOTIFICATION_ACTION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";

    static final String DW_BARCODE_DATA_EXTRA = "com.symbol.datawedge.data_string";
    static final String DW_BARCODE_TYPE_EXTRA = "com.symbol.datawedge.label_type";
    static final String DW_API_PARAM_EXTRA = "com.symbol.datawedge.api.SWITCH_SCANNER_PARAMS";
    static final String DW_API_CMD_ID_EXTRA = "COMMAND_IDENTIFIER";
    static final String DW_API_GET_STATUS_EXTRA = "com.symbol.datawedge.api.GET_SCANNER_STATUS";
    static final String DW_NOTIFICATION_EXTRA = "com.symbol.datawedge.api.NOTIFICATION";
    static final String DW_NOTIFICATION_TYPE_EXTRA = "NOTIFICATION_TYPE";
    static final String DW_NOTIFICATION_CHANGE_STATUS = "SCANNER_STATUS";
    static final String DW_NOTIFICATION_CHANGE_PROFILE = "PROFILE_SWITCH";
    static final String DW_NOTIFICATION_CHANGE_WORKFLOW = "WORKFLOW_STATUS";
    static final String DW_NOTIFICATION_CHANGE_CONFIGURATION = "CONFIGURATION_UPDATE";

}
