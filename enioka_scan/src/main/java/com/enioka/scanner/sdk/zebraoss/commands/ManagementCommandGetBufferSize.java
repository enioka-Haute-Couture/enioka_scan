package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.zebraoss.SsiPacket;
import com.enioka.scanner.sdk.zebraoss.data.RsmAttributeReply;

import java.nio.ByteBuffer;

/**
 * Special management command which should be sent before all others, requesting the RSM buffer size.
 */
public class ManagementCommandGetBufferSize extends SsiPacket implements Command<RsmAttributeReply> {
    private static final String LOG_TAG = "ManagementCommandGetBufferSize";

    public ManagementCommandGetBufferSize() {
        super((byte) (0x80 & 0xFF), new byte[]{(byte) 0x00, (byte) (0x06 & 0xFF), (byte) (0x20 & 0xFF), 0x00, (byte) 0xFF, (byte) 0xFF});
    }

    @Override
    public Class<? extends RsmAttributeReply> getReturnType() {
        return RsmAttributeReply.class;
    }
}
