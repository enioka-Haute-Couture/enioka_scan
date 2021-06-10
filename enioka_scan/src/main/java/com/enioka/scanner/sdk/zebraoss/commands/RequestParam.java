package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.ParamSend;
import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;

/**
 * Requets all params 0xC7 opcode, 0xFE request data)
 */
public class RequestParam extends SsiPacket implements Command<com.enioka.scanner.sdk.zebraoss.data.ParamSend> {
    private static final String LOG_TAG = "RequestParam";

    public RequestParam() {
        super((byte) (-57), new byte[]{(byte) (-2)});
    } // 0xC7 => -57, -2 is 0xFE when signed... sigh.

    public RequestParam(int prmCode) {
        super((byte) (-57), prmCodeToByteArray(prmCode));
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
    public Class<? extends com.enioka.scanner.sdk.zebraoss.data.ParamSend> getReturnType() {
        return ParamSend.class;
    }

    @Override
    public int getTimeOut() {
        return 5000;
    }
}
