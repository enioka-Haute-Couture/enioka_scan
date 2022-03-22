package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

/**
 * Equivalent to releasing the trigger.
 */
public class StopSession extends CommandExpectingAck {
    public StopSession(boolean isBle) {
        super(SsiCommand.STOP_SESSION.getOpCode(), isBle);
    }
}
