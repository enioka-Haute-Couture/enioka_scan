package com.enioka.scanner.sdk.zebraoss.commands;

import com.enioka.scanner.sdk.zebraoss.ssi.SsiCommand;

public class TempCommand extends CommandExpectingNothing {
    public TempCommand() {
        super(SsiCommand.TEMP_COMMAND.getOpCode());
    }
}
