package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class FlushQueue extends CommandExpectingNothing {

    public FlushQueue() {
        super((byte) 0xD2);
    }
}
