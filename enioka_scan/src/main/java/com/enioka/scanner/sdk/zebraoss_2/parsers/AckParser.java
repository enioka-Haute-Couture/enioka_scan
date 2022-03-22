package com.enioka.scanner.sdk.zebraoss_2.parsers;

import com.enioka.scanner.sdk.zebraoss_2.data.Ack;

/**
 * Responsible for handling ACK data
 */
public class AckParser implements PayloadParser<Ack> {
    @Override
    public Ack parseData(final byte[] dataBuffer) {
        return new Ack();
    }
}
