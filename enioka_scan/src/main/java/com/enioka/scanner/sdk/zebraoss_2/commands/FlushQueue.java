package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class FlushQueue extends CommandExpectingNothing {
    public FlushQueue(boolean isBle) {
        super(SsiCommand.FLUSH_QUEUE.getOpCode(), isBle);
    }
}
