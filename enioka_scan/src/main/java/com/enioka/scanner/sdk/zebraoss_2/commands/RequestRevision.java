package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.data.ReplyRevision;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

public class RequestRevision implements Command<ReplyRevision> {
    private final SsiMonoPacket packet;

    public RequestRevision(boolean isBle) {
        packet = new SsiMonoPacket(SsiCommand.REQUEST_REVISION.getOpCode(), SsiStatus.DEFAULT.getByte(), new byte[0], isBle);
    }

    @Override
    public byte[] getCommand() {
        return packet.toCommandBuffer();
    }

    @Override
    public Class<? extends ReplyRevision> getReturnType() {
        return ReplyRevision.class;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
