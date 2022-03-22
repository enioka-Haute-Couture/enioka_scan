package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.data.CapabilitiesReply;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

public class CapabilitiesRequest implements Command<CapabilitiesReply> {
    private final SsiMonoPacket packet;

    public CapabilitiesRequest(boolean isBle) {
        packet = new SsiMonoPacket(SsiCommand.CAPABILITIES_REQUEST.getOpCode(), SsiStatus.DEFAULT.getByte(), new byte[0], isBle);
    }

    @Override
    public byte[] getCommand() {
        return packet.toCommandBuffer();
    }

    @Override
    public Class<? extends CapabilitiesReply> getReturnType() {
        return CapabilitiesReply.class;
    }

    @Override
    public int getTimeOut() {
        return 2000;
    }
}
