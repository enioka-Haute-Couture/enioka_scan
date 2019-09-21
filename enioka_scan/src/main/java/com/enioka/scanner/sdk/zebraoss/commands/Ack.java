package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class Ack extends CommandExpectingNothing {
    public Ack() {
        super((byte) 0xD0);
    }
}
