package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

/**
 * Equivalent to releasing the trigger.
 */
public class StopSession extends CommandExpectingAck {
    public StopSession() {
        super(SsiCommand.STOP_SESSION.getOpCode());
    }
}
