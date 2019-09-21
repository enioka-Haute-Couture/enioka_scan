package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class ScanEnable extends CommandExpectingAck {

    public ScanEnable() {
        super((byte) 0xE9);
    }
}
