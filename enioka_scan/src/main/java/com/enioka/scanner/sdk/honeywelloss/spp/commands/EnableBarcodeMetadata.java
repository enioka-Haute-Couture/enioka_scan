package com.enioka.scanner.sdk.honeywelloss.spp.commands;

import com.enioka.scanner.bt.api.Command;

public class EnableBarcodeMetadata implements Command<Void> {
    @Override
    public byte[] getCommand() {
        // DECHDR1 / 0
        // SYN M CR ... 1 .
        return new byte[]{22, 77, 13, 68, 69, 67, 72, 68, 82, 49, 46};
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
