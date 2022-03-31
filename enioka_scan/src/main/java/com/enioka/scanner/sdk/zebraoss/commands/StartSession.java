package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

/**
 * Equivalent to pulling the trigger.
 */
public class StartSession extends CommandExpectingAck {
    public StartSession() {
        super(SsiCommand.START_SESSION.getOpCode());
    }
}
