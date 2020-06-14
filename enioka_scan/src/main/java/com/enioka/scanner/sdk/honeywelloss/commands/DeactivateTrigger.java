package com.enioka.scanner.sdk.honeywelloss.commands;

import com.enioka.scanner.bt.api.Command;

public class DeactivateTrigger implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // SYN U CR
        return new byte[]{0x16, 0x55, 0x0D};
    }

    @Override
    public Class<? extends Void> getReturnType() {
        return null;
    }

    @Override
    public int getTimeOut() {
        return 0;
    }
}
