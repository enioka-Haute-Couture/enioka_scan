package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.bt.LogHelpers;
import com.enioka.scanner.sdk.zebraoss.data.CapabilitiesReply;

import java.util.HashSet;
import java.util.Set;

/**
 * CAPABILITIES_REPLY parser.
 */
public class GenericParser implements PayloadParser<String> {
    private static final String LOG_TAG = "GenericParser";

    @Override
    public String parseData(byte[] buffer) {
        return LogHelpers.byteArrayToHex(buffer, buffer.length);
    }
}
