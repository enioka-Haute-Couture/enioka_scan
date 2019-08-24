package com.enioka.scanner.sdk.zebraoss.parsers;

import android.util.Log;

import com.enioka.scanner.bt.MessageRejectionReason;
import com.enioka.scanner.sdk.zebraoss.SsiMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CAPABILITIES_REPLY parser.
 */
public class CapabilitiesParser implements PayloadParser<Set<Byte>> {
    private static final String LOG_TAG = "CapabilitiesParser";

    @Override
    public Set<Byte> parseData(byte[] buffer) {
        if (buffer.length < 4) {
            return null;
        }

        Set<Byte> res = new HashSet<>(buffer.length - 4);
        for (int i = 4; i < buffer.length; i++) {
            res.add(buffer[i]);

            SsiMessage message = SsiMessage.GetValue(buffer[i]);
            if (message != SsiMessage.NONE) {
                Log.i(LOG_TAG, "Device supports command " + message.name());
            } else {
                Log.i(LOG_TAG, "Device supports unknown command " + String.format("0x%02x ", buffer[i]));
            }
        }

        return res;
    }
}
