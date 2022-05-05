package com.enioka.scanner.sdk.honeywelloss.spp.commands;

import com.enioka.scanner.bt.api.Command;

public class DisableIllumination implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // SCNLED0 / 1
        // SYN M CR S C N L E D 0 !
        return new byte[]{22, 77, 13, 83, 67, 78, 76, 69, 68, 48, 33};
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
