package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.bt.api.Helpers;

/**
 * CAPABILITIES_REPLY parser.
 */
public class GenericParser implements PayloadParser<String> {
    private static final String LOG_TAG = "GenericParser";

    @Override
    public String parseData(byte[] buffer) {
        return Helpers.byteArrayToHex(buffer, buffer.length);
    }
}
