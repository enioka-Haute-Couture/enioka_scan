package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.data.RsmAttributeReply;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

/**
 * Request a specific RSM attribute or a list of RSM attributes.
 */
public class ManagementCommandGetAttribute implements Command<RsmAttributeReply> {
    private final SsiMonoPacket packet;

    public ManagementCommandGetAttribute(boolean isBle, int... prmCodes) {
        packet = new SsiMonoPacket(SsiCommand.SSI_MGMT_COMMAND.getOpCode(), SsiStatus.DEFAULT.getByte(), prmCodesToByteArray(prmCodes), isBle);
    }

    private static byte[] prmCodesToByteArray(int[] prmCodes) {
        int length = prmCodes.length * 2 + 4;
        byte[] buffer = new byte[length];

        // First two bytes: RSM length
        buffer[0] = (byte) (length >>> 8);
        buffer[1] = (byte) (length);

        // Thirst byte: opcode. 2 is for attr get.
        buffer[2] = 0x2;

        // 4th byte is likely a status of sort. 0 seems to be OK.
        buffer[3] = 0x0;

        // After that, two bytes per prmCode requested.
        int idx = 3;
        for (int code : prmCodes) {
            buffer[++idx] = (byte) (code >>> 8);
            buffer[++idx] = (byte) (code);
        }

        return buffer;
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
        return 5000;
    }
}
