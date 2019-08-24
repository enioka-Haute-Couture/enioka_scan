package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

public class CapabilitiesRequest extends SsiPacket implements ICommand<Void> {
    private static final String LOG_TAG = "CapabilitiesRequest";

    public CapabilitiesRequest() {
        super((byte) 0xD3, new byte[]{});
    }

    @Override
    public byte[] getCommand() {
        return this.getMessageData();
    }

    @Override
    public CommandCallback<Void> getCallback() {
        return null;
    }
}
