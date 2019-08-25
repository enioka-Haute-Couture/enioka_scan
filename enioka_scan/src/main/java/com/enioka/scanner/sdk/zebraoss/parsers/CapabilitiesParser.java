package com.enioka.scanner.sdk.zebraoss.parsers;

import android.util.Log;

import com.enioka.scanner.bt.MessageRejectionReason;
import com.enioka.scanner.sdk.zebraoss.SsiMessage;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CAPABILITIES_REPLY parser.
 */
public class CapabilitiesParser implements PayloadParser<CapabilitiesReply> {
    private static final String LOG_TAG = "CapabilitiesParser";

    @Override
    public CapabilitiesReply parseData(byte[] buffer) {
        if (buffer.length < 4) {
            return null;
        }

        boolean supportsMultiPacket = buffer[3] == 1;

        Set<Byte> res = new HashSet<>(buffer.length - 4);
        for (int i = 4; i < buffer.length; i++) {
            res.add(buffer[i]);
        }

        return new CapabilitiesReply(supportsMultiPacket, res);
    }
}
