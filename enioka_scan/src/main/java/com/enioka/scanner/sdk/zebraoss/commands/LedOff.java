package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

public class LedOff extends SsiPacket implements ICommand<Void> {
    private static final String LOG_TAG = "LedOff";

    public LedOff() {
        super((byte) 0xE8, new byte[]{(byte) 0xFF});
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
