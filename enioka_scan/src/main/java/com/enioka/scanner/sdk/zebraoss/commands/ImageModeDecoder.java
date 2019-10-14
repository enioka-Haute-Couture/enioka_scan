package com.enioka.scanner.sdk.zebraoss.commands;

/**
 * A message or segment acknowledgment.
 */
public class ImageModeDecoder extends CommandExpectingAck {

    public ImageModeDecoder() {
        super((byte) 0xF7, new byte[]{0x00});
    }
}
