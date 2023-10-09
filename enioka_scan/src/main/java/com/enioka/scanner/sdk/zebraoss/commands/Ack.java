package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

/**
 * A message or segment acknowledgment.
 */
public class Ack extends CommandExpectingNothing {
    public Ack(boolean useHostAck) {
        super(useHostAck ? SsiCommand.HOST_ACK.getOpCode() : SsiCommand.CMD_ACK.getOpCode());
    }
}
