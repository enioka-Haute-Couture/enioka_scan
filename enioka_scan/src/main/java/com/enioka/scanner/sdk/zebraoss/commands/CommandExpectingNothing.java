package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiStatus;

public abstract class CommandExpectingNothing implements Command<Void> {
    private final SsiMonoPacket packet;

    public CommandExpectingNothing(byte opCode, byte[] data, boolean isBle) {
        packet = new SsiMonoPacket(opCode, SsiStatus.DEFAULT.getByte(), data, isBle);
    }

    public CommandExpectingNothing(byte opCode, boolean isBle) {
        packet = new SsiMonoPacket(opCode, SsiStatus.DEFAULT.getByte(), new byte[0], isBle);
    }

    @Override
    public byte[] getCommand() {
        return packet.toCommandBuffer();
    }

    @Override
    public Class<? extends Void> getReturnType() {
        return null;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
