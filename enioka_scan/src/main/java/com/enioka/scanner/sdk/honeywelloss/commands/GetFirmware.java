package com.enioka.scanner.sdk.honeywelloss.commands;

import com.enioka.scanner.bt.api.Command;
import com.enioka.scanner.sdk.honeywelloss.data.FirmwareVersion;

public class GetFirmware implements Command<FirmwareVersion> {
    @Override
    public byte[] getCommand() {
        // SYN M CR "REV" "INF" "^" "!"
        return new byte[]{22, 77, 13, 82, 69, 86, 73, 78, 70, 94, 33};
    }

    @Override
    public Class<? extends FirmwareVersion> getReturnType() {
        return FirmwareVersion.class;
    }

    @Override
    public int getTimeOut() {
        return 1000;
    }
}
