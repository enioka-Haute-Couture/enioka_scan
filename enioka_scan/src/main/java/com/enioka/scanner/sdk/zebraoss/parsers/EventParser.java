package com.enioka.scanner.sdk.zebraoss.parsers;

import com.enioka.scanner.sdk.zebraoss.data.Event;

/**
 * Responsible for handling NAK data
 */
public class EventParser implements PayloadParser<Event> {
    private static final String LOG_TAG = "ErrorParser";

    @Override
    public Event parseData(byte[] buffer) {
        if (buffer.length < 1) {
            return null;
        }

        return new Event(new byte[]{buffer[0]});
    }
}
