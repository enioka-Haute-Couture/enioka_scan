package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;

public abstract class CommandExpectingAck extends SsiPacket implements ICommand<Ack> {
    public CommandExpectingAck(byte opCode, byte[] data) {
        super(opCode, data);
    }

    CommandExpectingAck(byte opCode) {
        super(opCode);
    }

    @Override
    public Class<? extends Ack> getReturnType() {
        return Ack.class;
    }
}
