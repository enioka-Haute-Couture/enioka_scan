package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

/**
 * A message or segment acknowledgment.
 */
public class Ack extends CommandExpectingNothing {
    public Ack(boolean isBle) {
        super(SsiCommand.CMD_ACK.getOpCode(), isBle);
    }
}
