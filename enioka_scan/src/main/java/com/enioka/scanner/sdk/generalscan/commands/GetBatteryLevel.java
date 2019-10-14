package com.enioka.scanner.sdk.generalscan.commands;

import android.util.Log;


public class GetBatteryLevel  {

    public String getCommand() {
        return "{G1066}";
    }

    public void process(byte[] data, int offset, int length) {
        /*String res = translateToString(data, offset, length);
        String[] segments = res.split("/");
        if (segments.length < 2) {
            Log.e(LOG_TAG, "What the hell?");
            return;
        }
        Log.i(LOG_TAG, segments[2].replace("%]", "") + "% battery");*/
    }

    public void endOfTransmission() {

    }
}
