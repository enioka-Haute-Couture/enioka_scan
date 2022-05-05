package com.enioka.scanner.sdk.honeywelloss.spp.commands;

import com.enioka.scanner.bt.api.Command;

public class DisableAimer implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // SCNAIM0 / 2
        // SYN M CR ... 0 !
        return new byte[]{22, 77, 13, 83, 67, 78, 65, 73, 77, 48, 33};
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
