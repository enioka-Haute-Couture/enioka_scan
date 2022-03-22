package com.enioka.scanner.sdk.zebraoss_2.commands;

import com.enioka.scanner.sdk.zebraoss_2.ssi.SsiCommand;

public class ScanEnable extends CommandExpectingAck {
    public ScanEnable() {
        super(SsiCommand.SCAN_ENABLE.getOpCode());
    }
}
