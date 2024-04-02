package com.enioka.scanner.sdk.generalscan.commands;

/**
 * This does not work - FNC3+205080+level is a native Zebra command, understood by the Zebra decoder but not understood by the device controller.
 */
public class SetBeepLevel extends BaseCommandNoAck {
    public SetBeepLevel(int level) {
        if (level < 0) {
            level = 0;
        }
        if (level > 2) {
            level = 2;
        }
        level = 2 - level;

        this.stringCommand = "É205080" + level;
    }
}
