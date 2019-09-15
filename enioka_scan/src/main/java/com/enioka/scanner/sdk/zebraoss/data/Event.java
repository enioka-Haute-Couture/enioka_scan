package com.enioka.scanner.sdk.zebraoss.data;

import com.enioka.scanner.bt.LogHelpers;

public class Event {
    private byte[] data;

    public Event(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Event code " + LogHelpers.byteArrayToHex(data, data.length);
    }
}
