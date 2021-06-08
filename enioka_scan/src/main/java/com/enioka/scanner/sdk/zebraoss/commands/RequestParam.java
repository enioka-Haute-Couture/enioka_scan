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

    @Override
    public Class<? extends com.enioka.scanner.sdk.zebraoss.data.ParamSend> getReturnType() {
        return ParamSend.class;
    }

    @Override
    public int getTimeOut() {
        return 2000;
    }
}
