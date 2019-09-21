package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment acknowledgment.
 */
public class FlushQueue extends CommandExpectingNothing {

    public FlushQueue() {
        super((byte) 0xD2);
    }
}
