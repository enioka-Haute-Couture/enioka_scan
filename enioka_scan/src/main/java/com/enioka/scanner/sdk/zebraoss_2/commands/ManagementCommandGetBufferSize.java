package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.data.RsmAttributeReply;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

/**
 * Special management command which should be sent before all others, requesting the RSM buffer size.
 */
// FIXME: BLE value for packet constructor
public class ManagementCommandGetBufferSize implements Command<RsmAttributeReply> {
    private final SsiMonoPacket packet;

    public ManagementCommandGetBufferSize() {
        packet = new SsiMonoPacket(SsiCommand.SSI_MGMT_COMMAND.getOpCode(), SsiStatus.DEFAULT.getByte(),new byte[]{0x00, 0x06, 0x20, 0x00, (byte) 0xFF, (byte) 0xFF}, false);
    }

    @Override
    public byte[] getCommand() {
        return packet.toCommandBuffer();
    }

    @Override
    public Class<? extends RsmAttributeReply> getReturnType() {
        return RsmAttributeReply.class;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
