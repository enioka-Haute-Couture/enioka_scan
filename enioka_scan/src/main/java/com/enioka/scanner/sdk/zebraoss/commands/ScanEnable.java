package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class ScanEnable extends CommandExpectingAck {
    public ScanEnable(boolean isBle) {
        super(SsiCommand.SCAN_ENABLE.getOpCode(), isBle);
    }
}
