package com.enioka.scanner.sdk.generalscan.commands;

import android.util.Log;

import com.enioka.scanner.bt.BtCommand;
import com.enioka.scanner.bt.BtCommandWithAnswer;

public class SetBeepLevel extends BtCommand {
    @Override
    public String getCommand() {
        return "{G3008/0}";
    }
}
