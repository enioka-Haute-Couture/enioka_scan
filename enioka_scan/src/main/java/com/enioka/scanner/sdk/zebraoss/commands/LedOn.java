package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.api.Color;
import com.enioka.scanner.bt.CommandCallback;
import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

public class LedOn extends SsiPacket implements ICommand<Void> {
    private static final String LOG_TAG = "LedOn";

    public LedOn(Color color) {
        super((byte) 0xE7, new byte[]{getColorMask(color)});
    }

    private static byte getColorMask(Color color) {
        byte colorMask = 0b00000000;
        switch (color) {
            case GREEN:
            default:
                colorMask |= 0b00000001;
                break;
            case RED:
                colorMask |= 0b00000010;
                break;
        }
        return colorMask;
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
