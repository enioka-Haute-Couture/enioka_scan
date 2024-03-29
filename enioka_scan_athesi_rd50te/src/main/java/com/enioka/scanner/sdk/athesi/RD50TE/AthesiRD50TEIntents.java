package com.enioka.scanner.sdk.athesi.RD50TE;

/**
 * Intent actions, extras and related constants for the Athesi E5L integrated scanner, observed through android error logs.
 */
public class AthesiRD50TEIntents {
    public static final String BARCODE_EVENT = "com.android.serial.BARCODEPORT_RECEIVEDDATA_ACTION";
    public static final String BARCODE_DATA_EXTRA = "DATA";
    public static final String PRESS_TRIGGER = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_L_DOWN";
    public static final String RELEASE_TRIGGER = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_L_UP";
}
