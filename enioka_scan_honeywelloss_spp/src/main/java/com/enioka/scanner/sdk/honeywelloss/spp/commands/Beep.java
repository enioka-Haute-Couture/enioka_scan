package com.enioka.scanner.sdk.honeywelloss.spp.commands;

import com.enioka.scanner.bt.api.Command;

public class Beep implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // SYN BELL CR
        return new byte[]{0x16, 0x07, 0x0D};
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
