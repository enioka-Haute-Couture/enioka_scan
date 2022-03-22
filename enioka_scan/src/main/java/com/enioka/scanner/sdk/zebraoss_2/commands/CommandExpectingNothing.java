package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

// FIXME: BLE value for packet constructor
public abstract class CommandExpectingNothing implements Command<Void> {
    private final SsiMonoPacket packet;

    public CommandExpectingNothing(byte opCode, byte[] data) {
        packet = new SsiMonoPacket(opCode, SsiStatus.DEFAULT.getByte(), data, false);
    }

    public CommandExpectingNothing(byte opCode) {
        packet = new SsiMonoPacket(opCode, SsiStatus.DEFAULT.getByte(), new byte[0], false);
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
