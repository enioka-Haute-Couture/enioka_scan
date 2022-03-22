package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class ScanDisable extends CommandExpectingAck {
    public ScanDisable(boolean isBle) {
        super(SsiCommand.SCAN_DISABLE.getOpCode(), isBle);
    }
}
