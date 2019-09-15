package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment acknowledgment.
 */
public class StartSession extends SsiPacket implements ICommand<Void> {

    public StartSession() {
        super((byte) 0xE4, new byte[]{});
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
