package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

public class CapabilitiesRequest extends SsiPacket implements Command<CapabilitiesReply> {
    public CapabilitiesRequest() {
        super((byte) 0xD3);
    }

    @Override
    public Class<? extends CapabilitiesReply> getReturnType() {
        return CapabilitiesReply.class;
    }
}
