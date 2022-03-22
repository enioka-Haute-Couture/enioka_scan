package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class LedOff extends CommandExpectingAck {
    public LedOff(boolean isBle) {
        super(SsiCommand.LED_OFF.getOpCode(), new byte[]{(byte) 0xFF}, isBle);
    }
}
