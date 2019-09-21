package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class StartSession extends CommandExpectingNothing {

    public StartSession() {
        super((byte) 0xE4);
    }
}
