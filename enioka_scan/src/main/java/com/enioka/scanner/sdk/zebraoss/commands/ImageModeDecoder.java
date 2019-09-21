package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

/**
 * A message or segment acknowledgment.
 */
public class ImageModeDecoder extends CommandExpectingAck {

    public ImageModeDecoder() {
        super((byte) 0xF7, new byte[]{0x00});
    }
}
