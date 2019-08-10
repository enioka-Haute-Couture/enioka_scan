package com.enioka.scanner.sdk.generalscan.commands;

import android.util.Log;

import com.enioka.scanner.bt.BtCommandWithAnswer;

public class GetBatteryLevel extends BtCommandWithAnswer {
    @Override
    public String getCommand() {
        return "{G1066}";
    }

    @Override
    public void process(byte[] data, int offset, int length) {
        String res = translateToString(data, offset, length);
        String[] segments = res.split("/");
        if (segments.length < 2) {
            Log.e(LOG_TAG, "What the hell?");
            return;
        }
        Log.i(LOG_TAG, segments[2].replace("%]", "") + "% battery");
    }

    @Override
    public void endOfTransmission() {

    }
}
