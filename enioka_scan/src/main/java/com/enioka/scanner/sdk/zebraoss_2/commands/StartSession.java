package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

/**
 * Equivalent to pulling the trigger.
 */
public class StartSession extends CommandExpectingAck {
    public StartSession(boolean isBle) {
        super(SsiCommand.START_SESSION.getOpCode(), isBle);
    }
}
