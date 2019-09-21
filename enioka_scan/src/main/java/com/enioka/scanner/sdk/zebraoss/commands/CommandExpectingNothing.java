package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

public abstract class CommandExpectingNothing extends SsiPacket implements ICommand<Void> {
    public CommandExpectingNothing(byte opCode, byte[] data) {
        super(opCode, data);
    }

    CommandExpectingNothing(byte opCode) {
        super(opCode);
    }

    @Override
    public Class<? extends Void> getReturnType() {
        return null;
    }
}
