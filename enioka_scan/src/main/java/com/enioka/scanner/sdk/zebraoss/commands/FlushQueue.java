package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class FlushQueue extends CommandExpectingNothing {
    public FlushQueue(boolean isBle) {
        super(SsiCommand.FLUSH_QUEUE.getOpCode(), isBle);
    }
}
