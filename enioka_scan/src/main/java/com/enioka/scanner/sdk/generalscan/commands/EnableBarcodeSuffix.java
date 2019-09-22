package com.enioka.scanner.sdk.generalscan.commands;

/**
 * The library needs  suffix on all barcode returns, including barcode data (only place where it is optional).
 */
public class EnableBarcodeSuffix extends BaseCommandNoAck {
    public EnableBarcodeSuffix() {
        this.stringCommand = "{2043/\r\n}";
    }
}
