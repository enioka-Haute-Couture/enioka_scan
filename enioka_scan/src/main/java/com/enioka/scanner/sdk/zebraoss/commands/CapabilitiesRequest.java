package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

public class CapabilitiesRequest extends SsiPacket implements ICommand<CapabilitiesReply> {
    private static final String LOG_TAG = "CapabilitiesRequest";

    private CommandCallback<CapabilitiesReply> callback;

    public CapabilitiesRequest(CommandCallback<CapabilitiesReply> callback) {
        super((byte) 0xD3, new byte[]{});
        this.callback = callback;
    }

    @Override
    public byte[] getCommand() {
        return this.getMessageData();
    }

    @Override
    public CommandCallbackHolder<CapabilitiesReply> getCallback() {
        return new CommandCallbackHolder<>(CapabilitiesReply.class, this.callback, false, 100);
    }
}
