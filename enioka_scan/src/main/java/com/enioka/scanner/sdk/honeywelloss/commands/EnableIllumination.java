package com.enioka.scanner.sdk.honeywelloss.commands;

import com.enioka.scanner.bt.api.Command;

public class EnableIllumination implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // SCNLED0 / 1
        // SYN M CR S C N L E D 1 !
        return new byte[]{22, 77, 13, 83, 67, 78, 76, 69, 68, 49, 33};
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
