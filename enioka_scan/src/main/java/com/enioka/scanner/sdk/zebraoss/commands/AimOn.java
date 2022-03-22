package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class AimOn extends CommandExpectingAck {
    public AimOn(boolean isBle) {
        super(SsiCommand.AIM_ON.getOpCode(), isBle);
    }
}
