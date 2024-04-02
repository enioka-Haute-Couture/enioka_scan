package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class ScanDisable extends CommandExpectingAck {
    public ScanDisable() {
        super(SsiCommand.SCAN_DISABLE.getOpCode());
    }
}
