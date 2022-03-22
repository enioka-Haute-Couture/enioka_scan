package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class LedOff extends CommandExpectingAck {
    public LedOff(boolean isBle) {
        super(SsiCommand.LED_OFF.getOpCode(), new byte[]{(byte) 0xFF}, isBle);
    }
}
