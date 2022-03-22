package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class Beep extends CommandExpectingAck {
    public Beep(byte beepCode) {
        super(SsiCommand.BEEP.getOpCode(), new byte[]{beepCode});
    }
}
