package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class ImageModeDecoder extends CommandExpectingAck {
    public ImageModeDecoder() {
        super(SsiCommand.IMAGER_MODE.getOpCode(), new byte[]{0x00});
    }
}
