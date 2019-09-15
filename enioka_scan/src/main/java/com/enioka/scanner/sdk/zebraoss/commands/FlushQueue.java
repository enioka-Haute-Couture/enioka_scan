package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment acknowledgment.
 */
public class FlushQueue extends SsiPacket implements ICommand<Void> {

    public FlushQueue() {
        super((byte) 0xD2, new byte[]{});
    }

    @Override
    public byte[] getCommand() {
        return this.getMessageData();
    }

    @Override
    public CommandCallbackHolder<Void> getCallback() {
        return null;
    }
}
