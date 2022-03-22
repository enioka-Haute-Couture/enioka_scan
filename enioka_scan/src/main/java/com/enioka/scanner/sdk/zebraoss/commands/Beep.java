package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class Beep extends CommandExpectingAck {
    public Beep(byte beepCode, boolean isBle) {
        super(SsiCommand.BEEP.getOpCode(), new byte[]{beepCode}, isBle);
    }
}
