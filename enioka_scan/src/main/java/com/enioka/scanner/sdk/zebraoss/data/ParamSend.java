package com.enioka.scanner.sdk.zebraoss.data;

import com.enioka.scanner.bt.api.Helpers;

/**
 * Answer to a parameter value query.
 */
public class ParamSend {
    private byte[] data;

    public ParamSend(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Event code " + Helpers.byteArrayToHex(data, data.length);
    }
}
