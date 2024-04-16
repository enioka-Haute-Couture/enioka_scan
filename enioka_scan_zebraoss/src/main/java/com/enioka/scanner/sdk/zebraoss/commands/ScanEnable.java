package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class ScanEnable extends CommandExpectingAck {
    public ScanEnable() {
        super(SsiCommand.SCAN_ENABLE.getOpCode());
    }
}
