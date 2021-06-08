package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.bt.api.Helpers;
import com.enioka.scanner.sdk.zebraoss.SsiMultiPacketMessage;

/**
 * CAPABILITIES_REPLY parser.
 */
public class GenericParser implements PayloadParser<String> {
    @Override
    public String parseData(SsiMultiPacketMessage message) {
        byte[] buffer = message.getData();
        return Helpers.byteArrayToHex(buffer, buffer.length);
    }
}
