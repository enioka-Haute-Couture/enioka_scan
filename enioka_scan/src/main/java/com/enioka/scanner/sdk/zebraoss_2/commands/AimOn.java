package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class AimOn extends CommandExpectingAck {
    public AimOn() {
        super(SsiCommand.AIM_ON.getOpCode());
    }
}
