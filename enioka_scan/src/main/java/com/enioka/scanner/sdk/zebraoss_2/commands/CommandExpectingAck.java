package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

// FIXME: BLE value for packet constructor
public abstract class CommandExpectingAck implements Command<Ack> {
    private final SsiMonoPacket packet;

    public CommandExpectingAck(byte opCode, byte[] data) {
        packet = new SsiMonoPacket(opCode, SsiStatus.DEFAULT.getByte(), data, false);
    }

    public CommandExpectingAck(byte opCode) {
        packet = new SsiMonoPacket(opCode, SsiStatus.DEFAULT.getByte(), new byte[0], false);
    }

    @Override
    public byte[] getCommand() {
        return packet.toCommandBuffer();
    }

    @Override
    public Class<? extends Ack> getReturnType() {
        return Ack.class;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
