package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

import java.util.HashSet;
import java.util.Set;

/**
 * CAPABILITIES_REPLY parser.
 */
public class CapabilitiesParser implements PayloadParser<CapabilitiesReply> {
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
