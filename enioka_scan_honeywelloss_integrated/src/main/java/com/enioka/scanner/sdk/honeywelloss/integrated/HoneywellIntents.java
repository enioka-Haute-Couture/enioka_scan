package com.enioka.scanner.sdk.honeywelloss.integrated;

public class HoneywellIntents {

    /** CLAIM SCANNER action **/
    public static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";
    /** Extra for CLAIM_SCANNER. Selects a profile to load properties. Value type: String. Either "DEFAULT" for factory settings or the name of the profile to use. */
    public static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    /** Value of EXTRA_PROFILE, uses the factory settings. */
    public static final String EXTRA_PROFILE_USE_DEFAULT = "DEFAULT";
    /** Extra for CLAIM_SCANNER. Overrides the default behavior for selecting the scanner. Value type: String. **/
    public static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    /** Value of EXTRA_SCANNER, forces the use of the integrated scanner */
    public static final String EXTRA_SCANNER_USE_IMAGER = "dcs.scanner.imager";
    /** Value of EXTRA_SCANNER, forces the use of an external ring scanner */
    public static final String EXTRA_SCANNER_USE_RING = "dcs.scanner.ring";
    /** Extra for CLAIM_SCANNER. Overrides properties set by the profile (see official documentation). Value type: Bundle. */
    public static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";

    // Properties used during setup to tell the scanner what intent action to use when notifying of barcode data
    public static final String PROPERTY_DATA_INTENT_BOOLEAN = "DPR_DATA_INTENT";
    public static final String PROPERTY_DATA_INTENT_ACTION = "DPR_DATA_INTENT_ACTION";

    /** RELEASE SCANNER action */
    public static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";

    /** CONTROL SCANNER action, for software control of the scanner (physical control unaffected) */
    public static final String ACTION_CONTROL_SCANNER = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER";
    /** Extra for CONTROL_SCANNER. True if the scanner must start scanning, false if the scanner must stop scanning. Value type: boolean. Mandatory. */
    public static final String EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN";
    /** Extra for CONTROL_SCANNER. True if the aim-assist light must be turned on, false if the aim-assist light must be turned off. Value type: boolean. Defaults to EXTRA_SCAN value. */
    public static final String EXTRA_AIM = "com.honeywell.aidc.extra.EXTRA_AIM";
    /** Extra for CONTROL_SCANNER. True if the illumination must be turned on, false if the illumination must be turned off. Value type: boolean. Defaults to EXTRA_SCAN value. */
    public static final String EXTRA_LIGHT = "com.honeywell.aidc.extra.EXTRA_LIGHT";
    /** Extra for CONTROL_SCANNER. True if the scanner must decode data, false if the scanner must not decode data. Value type: boolean. Defaults to EXTRA_SCAN value. */
    public static final String EXTRA_DECODE = "com.honeywell.aidc.extra.EXTRA_DECODE";

    /** READ EVENT action (received when the scanner successfully scans a barcode) */
    public static final String EVENT_BARCODE_READ = "com.honeywell.aidc.action.ACTION_BARCODE_READ_EVENT";
    /** Extra for READ_EVENT. The AIM symbology identifier. Value type: String. */
    public static final String EXTRA_AIMID = "aimId";
    /** Extra for READ_EVENT. The charset used to encode the raw data into a string. Value type: String. */
    public static final String EXTRA_CHARSET = "charset";
    /** Extra for READ_EVENT. The Honeywell symbology identifier. Value type: String. */
    public static final String EXTRA_CODEID = "codeId";
    /** Extra for READ_EVENT. The barcode data string. Value type: String. */
    public static final String EXTRA_DATA = "data";
    /** Extra for READ_EVENT. The raw barcode data. Value type: byte[]. */
    public static final String EXTRA_DATABYTES = "dataBytes";
    /** Extra for READ_EVENT. The timestamp of the barcode data. Value type: String. */
    public static final String EXTRA_TIMESTAMP = "timestamp";
    /** Extra for READ_EVENT. Always "1" (apparently). Value type: int. */
    public static final String EXTRA_VERSION = "version";
    /** Extra for READ_EVENT. Name of the scanner that read the barcode. Value type: String. */
    public static final String EXTRA_NAME = "scanner";
}
