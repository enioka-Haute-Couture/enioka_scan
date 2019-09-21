package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class ScanDisable extends CommandExpectingAck {

    public ScanDisable() {
        super((byte) 0xEA);
    }
}
