package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class ActivateAllSymbologies extends CommandExpectingNothing {
    public ActivateAllSymbologies() {
        super(SsiCommand.CHANGE_ALL_CODE_TYPES.getOpCode(), new byte[]{0x01});
    }
}
