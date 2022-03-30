package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.bt.api.Scanner;
import com.enioka.scanner.sdk.zebraoss.data.ParamSend;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiMonoPacketWrapper;
import com.enioka.scanner.sdk.zebraoss.ssi.SsiStatus;

/**
 * Requests all params 0xC7 opcode, 0xFE request data)
 */
public class RequestParam implements Command<ParamSend> {
    private final SsiMonoPacketWrapper packet;

    public RequestParam() {
        packet = new SsiMonoPacketWrapper(SsiCommand.PARAM_REQUEST.getOpCode(), SsiStatus.DEFAULT.getByte(), new byte[]{(byte) (0xFE)});
    }

    public RequestParam(int prmCode) {
        packet = new SsiMonoPacketWrapper(SsiCommand.PARAM_REQUEST.getOpCode(), SsiStatus.DEFAULT.getByte(), prmCodeToByteArray(prmCode));
    }

    private static byte[] prmCodeToByteArray(int prmCode) {
        byte[] prmBytes;
        if (prmCode >= 1024) {
            prmBytes = new byte[]{(byte) (0xF8 & 0xFF), (byte) (prmCode >>> 8), (byte) prmCode};
        } else if (prmCode >= 768) {
            prmBytes = new byte[]{(byte) (0xF2 & 0xFF), (byte) (prmCode - 768)};
        } else if (prmCode >= 512) {
            prmBytes = new byte[]{(byte) (0xF1 & 0xFF), (byte) (prmCode - 512)};
        } else if (prmCode >= 256) {
            prmBytes = new byte[]{(byte) (0xF0 & 0xFF), (byte) (prmCode - 256)};
        } else {
            prmBytes = new byte[]{(byte) prmCode};
        }
        return prmBytes;
    }

    @Override
    public byte[] getCommand() {
        return packet.toCommandBuffer(false);
    }

    @Override
    public byte[] getCommand(final Scanner scanner) {
        return packet.toCommandBuffer(scanner.isBleDevice());
    }

    @Override
    public Class<? extends ParamSend> getReturnType() {
        return ParamSend.class;
    }

    @Override
    public int getTimeOut() {
        return 5000;
    }
}
