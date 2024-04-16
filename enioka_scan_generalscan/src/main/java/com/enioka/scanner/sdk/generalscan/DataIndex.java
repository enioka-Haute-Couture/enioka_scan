package com.enioka.scanner.sdk.generalscan;

import com.enioka.scanner.sdk.generalscan.data.DeviceId;

import java.util.HashMap;
import java.util.Map;

/**
 * An index for all data parser classes.
 */
class DataIndex {
    static Map<String, Class<? extends Object>> parsers = new HashMap<>();

    static {
        parsers.put("[", DeviceId.class);
    }
}
