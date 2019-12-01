package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.data.Event;

/**
 * Responsible for handling NAK data
 */
public class ParamSendParser implements PayloadParser<Event> {
    @Override
    public Event parseData(byte[] buffer) {
        if (buffer.length < 1) {
            return null;
        }

        return new Event(new byte[]{buffer[0]});
    }
}
