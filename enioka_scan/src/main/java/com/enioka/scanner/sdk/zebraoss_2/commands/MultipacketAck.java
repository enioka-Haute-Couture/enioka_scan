package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

/**
 * A multipacket segment acknowledgment.
 */
public class MultipacketAck extends CommandExpectingNothing {
    public MultipacketAck(boolean isBle) {
        super(SsiCommand.MULTIPACKET_ACK.getOpCode(), isBle);
    }
}
