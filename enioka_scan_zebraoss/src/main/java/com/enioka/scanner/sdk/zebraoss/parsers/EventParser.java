package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.data.Event;

/**
 * Responsible for handling NAK data
 */
public class EventParser implements PayloadParser<Event> {
    @Override
    public Event parseData(final byte[] dataBuffer) {
        if (dataBuffer.length < 1) {
            return null;
        }

        return new Event(new byte[]{dataBuffer[0]});
    }
}
