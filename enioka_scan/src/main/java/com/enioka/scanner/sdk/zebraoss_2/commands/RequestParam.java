package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss_2.data.ParamSend;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiMonoPacket;
import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiStatus;

/**
 * Requests all params 0xC7 opcode, 0xFE request data)
 */
// FIXME: BLE value for packet constructor
public class RequestParam implements Command<ParamSend> {
    private final SsiMonoPacket packet;

    public RequestParam() {
        packet = new SsiMonoPacket(SsiCommand.PARAM_REQUEST.getOpCode(), SsiStatus.DEFAULT.getByte(), new byte[]{(byte) (0xFE)}, false);
    }

    public RequestParam(int prmCode) {
        packet = new SsiMonoPacket(SsiCommand.PARAM_REQUEST.getOpCode(), SsiStatus.DEFAULT.getByte(), prmCodeToByteArray(prmCode), false);
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
        return packet.toCommandBuffer();
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
