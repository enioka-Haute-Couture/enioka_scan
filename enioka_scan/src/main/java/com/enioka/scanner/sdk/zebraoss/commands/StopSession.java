package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * Equivalent to releasing the trigger.
 */
public class StopSession extends CommandExpectingAck {

    public StopSession() {
        super((byte) 0xE5);
    }
}
