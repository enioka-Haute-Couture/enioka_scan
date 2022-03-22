package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class ImageModeDecoder extends CommandExpectingAck {
    public ImageModeDecoder() {
        super(SsiCommand.IMAGER_MODE.getOpCode(), new byte[]{0x00});
    }
}
