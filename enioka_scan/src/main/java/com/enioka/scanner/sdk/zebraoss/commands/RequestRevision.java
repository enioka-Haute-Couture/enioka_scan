package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;

public class RequestRevision extends SsiPacket implements Command<ReplyRevision> {
    private static final String LOG_TAG = "RequestRevision";

    public RequestRevision() {
        super((byte) 0xA3, new byte[0]);
    }

    @Override
    public Class<? extends ReplyRevision> getReturnType() {
        return ReplyRevision.class;
    }
}
