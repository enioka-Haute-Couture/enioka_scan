package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class AimOn extends CommandExpectingAck {

    public AimOn() {
        super((byte) 0xC5);
    }
}
