package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * Equivalent to pulling the trigger.
 */
public class StartSession extends CommandExpectingAck {

    public StartSession() {
        super((byte) 0xE4);
    }
}
