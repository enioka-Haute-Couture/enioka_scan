package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.CommandCallbackHolder;
import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment acknowledgment.
 */
public class ImageModeDecoder extends SsiPacket implements ICommand<Void> {

    public ImageModeDecoder() {
        super((byte) 0xF7, new byte[]{0x00});
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
