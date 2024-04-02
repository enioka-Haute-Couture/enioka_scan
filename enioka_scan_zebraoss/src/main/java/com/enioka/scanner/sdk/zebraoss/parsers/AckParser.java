package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.data.Ack;

/**
 * Responsible for handling ACK data
 */
public class AckParser implements PayloadParser<Ack> {
    @Override
    public Ack parseData(final byte[] dataBuffer) {
        return new Ack();
    }
}
