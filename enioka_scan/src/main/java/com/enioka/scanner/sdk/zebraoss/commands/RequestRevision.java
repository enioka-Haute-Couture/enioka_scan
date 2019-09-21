package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.ICommand;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.ReplyRevision;

public class RequestRevision extends SsiPacket implements ICommand<ReplyRevision> {
    private static final String LOG_TAG = "RequestRevision";

    public RequestRevision() {
        super((byte) 0xA3, new byte[0]);
    }

    @Override
    public Class<? extends ReplyRevision> getReturnType() {
        return ReplyRevision.class;
    }
}
