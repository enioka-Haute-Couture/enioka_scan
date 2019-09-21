package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;

public class RequestParam extends SsiPacket implements Command<ReplyRevision> {
    private static final String LOG_TAG = "LedOff";

    public RequestParam() {
        super((byte) (-57), new byte[]{(byte) (-2)});
    } // 0xC7 => -57, -2 is 0xFE when signed... sigh.


    @Override
    public Class<? extends ReplyRevision> getReturnType() {
        return ReplyRevision.class;
    }
}
