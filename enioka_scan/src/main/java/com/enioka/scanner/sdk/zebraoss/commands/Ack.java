package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

/**
 * A message or segment acknowledgment.
 */
public class Ack extends CommandExpectingNothing {
    public Ack(boolean isBle) {
        super(SsiCommand.CMD_ACK.getOpCode(), isBle);
    }
}
