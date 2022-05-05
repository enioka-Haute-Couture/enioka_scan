package com.enioka.scanner.sdk.honeywelloss.spp.commands;

import com.enioka.scanner.bt.api.Command;

public class Cleanup implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // "!"
        return new byte[]{33};
    }

    @Override
    public Class<? extends Void> getReturnType() {
        return null;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
