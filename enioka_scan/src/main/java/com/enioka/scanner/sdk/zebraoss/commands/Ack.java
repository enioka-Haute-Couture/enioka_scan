package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

/**
 * A message or segment acknowledgment.
 */
public class Ack extends SsiPacket implements ICommand<Void> {

    public Ack() {
        super((byte) 0xD0, new byte[]{});
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
