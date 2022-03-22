package com.enioka.scanner.sdk.zebraoss_2.parsers;

import com.enioka.scanner.bt.api.Helpers;

/**
 * CAPABILITIES_REPLY parser, also used for other data types with no dedicated parsers for now.
 */
public class GenericParser implements PayloadParser<String> {
    @Override
    public String parseData(final byte[] dataBuffer) {
        return Helpers.byteArrayToHex(dataBuffer, dataBuffer.length);
    }
}
