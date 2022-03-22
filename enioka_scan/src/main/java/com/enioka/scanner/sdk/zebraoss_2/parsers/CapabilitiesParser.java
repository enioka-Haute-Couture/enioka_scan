package com.enioka.scanner.sdk.zebraoss_2.parsers;

import com.enioka.scanner.sdk.zebraoss_2.data.CapabilitiesReply;

import java.util.HashSet;
import java.util.Set;

/**
 * CAPABILITIES_REPLY parser.
 */
public class CapabilitiesParser implements PayloadParser<CapabilitiesReply> {
    @Override
    public CapabilitiesReply parseData(final byte[] dataBuffer) {
        if (dataBuffer.length < 4) {
            return null;
        }

        boolean supportsMultiPacket = dataBuffer[3] == 1;

        Set<Byte> res = new HashSet<>(dataBuffer.length - 4);
        for (int i = 4; i < dataBuffer.length; i++) {
            res.add(dataBuffer[i]);
        }

        return new CapabilitiesReply(supportsMultiPacket, res);
    }
}
