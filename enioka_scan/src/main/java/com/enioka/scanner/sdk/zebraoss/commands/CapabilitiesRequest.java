package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiStatus;

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
